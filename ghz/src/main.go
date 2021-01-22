package main

import (
	"encoding/json"
	"fmt"
	"github.com/bojand/ghz/runner"
	"net/http"
	"strconv"
	"time"
)

type GrpcRequest struct {
	N int32 `json:"n"`
}

type ResponseData struct {
	Report *runner.Report
	Error error
}

type HttpError struct {
	ErrorMessage string `json:"message"`
}

type HttpReport struct {
	TotalSeconds uint `json:"total_seconds"`
	AverageResponseMs int64 `json:"average_response_ms"`
	FastestResponseMs int64 `json:"fastest_response_ms"`
	SlowestResponseMs int64 `json:"slowest_response_ms"`
	RequestsPerSecond float64 `json:"requests_per_second"`
	StatusCodeDist map[string]int `json:"status_code_dist"`
	StopReason string `json:"stop_reason"`
}

type HttpResult struct {
	Report *HttpReport `json:"report"`
	Error error `json:"error"`
}

type HttpSuccess struct {
	Results map[string]*HttpResult `json:"results"`
}

func writeError(w http.ResponseWriter, message string) {
	data, _ := json.Marshal(&HttpError{ErrorMessage: message})
	fmt.Fprintln(w, string(data))
}

func writeReports(w http.ResponseWriter, r map[string]ResponseData) {
	results := make(map[string]*HttpResult)
	
	for k, v := range r {
		result := &HttpResult{
			Error: v.Error,
		}
		if v.Error == nil {
			report := &HttpReport{
				TotalSeconds:      uint(v.Report.Total.Seconds()),
				AverageResponseMs: v.Report.Average.Milliseconds(),
				FastestResponseMs: v.Report.Fastest.Milliseconds(),
				SlowestResponseMs: v.Report.Slowest.Milliseconds(),
				RequestsPerSecond: v.Report.Rps,
				StatusCodeDist:    v.Report.StatusCodeDist,
				StopReason:        v.Report.EndReason.String(),
			}
			
			result.Report = report
		}
		results[k] = result
	}
	
	jsonResponse, _ :=json.Marshal(HttpSuccess{Results: results})
	fmt.Fprintln(w, string(jsonResponse))
}

func doTest(w http.ResponseWriter, req * http.Request) {
	w.Header()["Content-Type"] = []string{"application/json"}
	prod, err := strconv.ParseUint(req.URL.Query().Get("products"), 10, 0)
	if err != nil {
		writeError(w, "Provide product number to read")
		return
	}
	threads, err := strconv.ParseUint(req.URL.Query().Get("threads"), 10, 0)
	if err != nil {
		writeError(w, "Provide number of threads")
		return
	}
	durationParam, err := strconv.ParseUint(req.URL.Query().Get("duration"), 10, 0)
	if err != nil {
		writeError(w, "Provide duration in seconds")
		return
	}
	duration, err := time.ParseDuration(fmt.Sprintf("%ds", durationParam))
	if err != nil {
		writeError(w, "Provide duration in seconds")
		return
	}

	phpReport, phpErr := runner.Run(
		"magento.grpcTestApi.proto.Products.read",
		"mage-grpc-phpserver:9000",
		runner.WithProtoFile("magegrpc.proto", []string{}),
		runner.WithConcurrency(uint(threads)),
		runner.WithRunDuration(duration),
		runner.WithDurationStopAction("ignore"),
		runner.WithSkipFirst(10),
		runner.WithInsecure(true),
		runner.WithData(GrpcRequest{N: int32(prod)}),
	)
	javaReport, javaErr := runner.Run(
		"magento.grpcTestApi.proto.Products.read",
		"mage-grpc-javaserver:9000",
		runner.WithProtoFile("magegrpc.proto", []string{}),
		runner.WithConcurrency(uint(threads)),
		runner.WithRunDuration(duration),
		runner.WithDurationStopAction("ignore"),
		runner.WithSkipFirst(10),
		runner.WithInsecure(true),
		runner.WithData(GrpcRequest{N: int32(prod)}),
	)
	mageReport, mageErr := runner.Run(
		"magento.grpcTestApi.proto.Products.read",
		"catalogstorefront:9001",
		runner.WithProtoFile("magegrpc.proto", []string{}),
		runner.WithConcurrency(uint(threads)),
		runner.WithRunDuration(duration),
		runner.WithDurationStopAction("ignore"),
		runner.WithSkipFirst(10),
		runner.WithInsecure(true),
		runner.WithData(GrpcRequest{N: int32(prod)}),
	)

	response := make(map[string]ResponseData)
	response["php"] = ResponseData{
		Report:       phpReport,
		Error: phpErr,
	}
	response["java"] = ResponseData{
		Report:       javaReport,
		Error: javaErr,
	}
	response["mage"] = ResponseData{
		Report:       mageReport,
		Error: mageErr,
	}
	writeReports(w, response)
}

func main() {
	http.HandleFunc("/test", doTest)

	http.ListenAndServe(":8080", nil)
}
