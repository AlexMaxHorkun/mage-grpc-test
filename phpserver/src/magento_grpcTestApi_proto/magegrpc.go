package magento_grpcTestApi_proto

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/spiral/broadcast"
	"github.com/spiral/jobs/v2"
	grpc "github.com/spiral/php-grpc"
	grpc2 "google.golang.org/grpc"
	"math"
	"math/rand"
	"time"
)

const ID = "magegrpc"
var batches = []int{5000, 2000, 1000, 500, 250, 100}

type Service struct {
	queue  *jobs.Service
	pubsub *broadcast.Service
	randomizer *rand.Rand
}

type OptionMessage struct {
	Id string `json:"id"`
	Title string `json:"title"`
	Price float32 `json:"price"`
	Available bool `json:"available"`
}

type GeneratedProductMessage struct {
	Finished bool `json:"finished"`
	Success bool `json:"success"`
	ErrorMsg string `json:"error_msg"`
	Product *ProductMessage `json:"product"`
}

type JobResult struct {
	Message *GeneratedProductMessage
	Err *error
	Finished bool
}

type ClearMessage struct {
	Success bool `json:"success"`
	ErrorMsg string `json:"error_msg"`
}

type ProductMessage struct {
	Id string `json:"id"`
	Sku string `json:"sku"`
	Price float32 `json:"price"`
	Title string `json:"title"`
	Description string `json:"description"`
	ImgUrl string `json:"img_url"`
	Available bool `json:"available"`
	Options *[]OptionMessage `json:"options"`
}

type ProductsReadMessage struct {
	Success bool `json:"success"`
	ErrorMsg string `json:"error_msg"`
	Products *[]ProductMessage `json:"products"`
}

func (s *Service) Init(g *grpc.Service, q *jobs.Service, p *broadcast.Service) (bool, error) {
	s.queue = q
	s.pubsub = p
	s.randomizer = rand.New(rand.NewSource(time.Now().UnixNano()))

	return true, g.AddService(func(server *grpc2.Server) {
		RegisterProductsServer(server, s)
	})
}

func (s *Service) Generate(in *GenerateArg, stream Products_GenerateServer) error {
	batchSize := int(in.Number)
	if in.Async {
		for _, size := range batches {
			if size < int(in.Number) {
				batchSize = size
				if size * 4 < int(in.Number) {
					break
				}
			}
		}
	}
	batchCount := int(math.Ceil(float64(in.Number) / float64(batchSize)))

	//Batching
	fmt.Printf("Generating %d products in %d batches\n", in.Number, batchCount)
	var finished int
	results := make(chan *JobResult)
	for currentBatch := 1; currentBatch <= batchCount; currentBatch++ {
		number := batchSize
		if currentBatch * number > int(in.Number) {
			number = int(in.Number) - (currentBatch - 1) * number
		}

		go s.initGenerateJob(number, results)
	}

	for result := range results {
		if result.Message != nil {
			msg := result.Message
			if err := stream.Send(convertToGrpcProduct(msg.Product)); err != nil {
				return err
			}
		} else if result.Finished {
			finished++
			if finished == batchCount {
				fmt.Println("Finished sending generated products")
				return nil
			}
		} else if result.Err != nil {
			return *result.Err
		}
	}

	return nil
}

func (s *Service) Clear(context context.Context, in *ClearArg) (*Cleared, error) {
	client := s.pubsub.NewClient()
	defer client.Close()
	//Start topic
	topicId := fmt.Sprintf("mage_grpc_clear_topic_%d", s.randomizer.Int())
	if err := client.Subscribe(topicId); err != nil {
		return nil, err
	}
	//Start the background producer
	_, err := s.queue.Push(&jobs.Job{
		Job:     "app.job.ProductsClear",
		Payload: fmt.Sprintf(`{"requestID":"%s"}`, topicId),
		Options: &jobs.Options{},
	})
	if err != nil {
		return nil, err
	}
	//Listen
	clearedMsg := &ClearMessage{}
	jobMessage := <- client.Channel()
	if err := json.Unmarshal(jobMessage.Payload, clearedMsg); err != nil {
		return nil, err
	}
	if !clearedMsg.Success {
		return nil, errors.New(clearedMsg.ErrorMsg)
	}

	return &Cleared{}, nil
}

func (s *Service) Read(context context.Context, in *ReadRequest) (*ReadResponse, error) {
	client := s.pubsub.NewClient()
	defer client.Close()
	//Start topic
	topicId := fmt.Sprintf("mage_grpc_read_topic_%d", s.randomizer.Int())
	if err := client.Subscribe(topicId); err != nil {
		return nil, err
	}
	//Start the background producer
	_, err := s.queue.Push(&jobs.Job{
		Job:     "app.job.ProductsRead",
		Payload: fmt.Sprintf(`{"requestID":"%s", "limit":%d}`, topicId, in.N),
		Options: &jobs.Options{},
	})
	if err != nil {
		return nil, err
	}
	//Listen
	readMessage := &ProductsReadMessage{}
	jobMessage := <- client.Channel()
	if err := json.Unmarshal(jobMessage.Payload, readMessage); err != nil {
		return nil, err
	}
	if !readMessage.Success {
		return nil, errors.New(readMessage.ErrorMsg)
	}

	//Generate response
	response := &ReadResponse{
		Items: make([]*Product, 0),
	}
	for _, product := range *readMessage.Products {
		response.Items = append(response.Items, convertToGrpcProduct(&product))
	}
	return response, nil
}

func (s *Service) initGenerateJob(count int, results chan *JobResult) {
	client := s.pubsub.NewClient()
	defer client.Close()

	//Start topic
	topicId := fmt.Sprintf("mage_grpc_topic_%d", s.randomizer.Int())
	if err := client.Subscribe(topicId); err != nil {
		results <- &JobResult{
			Err: &err,
		}
		return
	}

	// start the background producer
	_, err := s.queue.Push(&jobs.Job{
		Job:     "app.job.Products",
		Payload: fmt.Sprintf(`{"requestID":"%s", "number":%d}`, topicId, count),
		Options: &jobs.Options{},
	})
	if err != nil {
		results <- &JobResult{
			Err: &err,
		}
		return
	}

	jobChan := client.Channel()
	// forward data from topic to stream
	for msgData := range jobChan {
		msg := &GeneratedProductMessage{}
		if err := json.Unmarshal(msgData.Payload, msg); err != nil {
			results <- &JobResult{
				Err: &err,
			}
			return
		}
		if msg.Finished {
			err := errors.New(msg.ErrorMsg)
			if !msg.Success {
				results <- &JobResult{
					Message:  nil,
					Err:      &err,
					Finished: true,
				}
				return
			}
			results <- &JobResult{
				Finished: true,
			}
			return
		}
		results <- &JobResult{
			Message: msg,
		}
	}
}

func convertToGrpcProduct(product *ProductMessage) *Product {
	options := make([]*Option, 0)
	result := &Product{
		Id:                   product.Id,
		Sku:                  product.Sku,
		Price:                product.Price,
		Title:                product.Title,
		Description:          product.Description,
		ImgUrl:               product.ImgUrl,
		Available:            product.Available,
	}
	for _, option := range *product.Options {
		options = append(options, &Option{
			Id:        option.Id,
			Title:     option.Title,
			Price:     option.Price,
			Available: option.Available,
		})
	}
	result.Options = options

	return result
}
