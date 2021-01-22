FROM golang:1.15

WORKDIR /go/src/app
COPY src /go/src/app

RUN rm go.sum
RUN go mod tidy
RUN go mod vendor
RUN go install -v main.go

CMD "main"
EXPOSE 8080
