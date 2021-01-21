1. Provide absolute path in volumes section for "grpc-testing-deployment" in ./kubes/mage-grpc-test.yaml
2. Run measurement with corresponding settings 
`kubectl exec --tty deployment/grpc-testing-deployment -- /go/bin/ghz --config=./test/ghz_config.json --duration 1m --concurrency=10 host:port > path/to/results`

Or simply run `bash ./do-grpc-testing.sh`