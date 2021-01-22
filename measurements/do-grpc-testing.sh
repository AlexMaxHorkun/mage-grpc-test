#!/usr/bin/env bash

mkdir -p results-grpc
duration=60s

for service in catalogstorefront:9001 mage-grpc-javaserver:9000 mage-grpc-phpserver:9000
do
  output="results-grpc/"$(echo $service| cut -d':' -f 1)
  mkdir -p ${output}
  for concurrency in $@
  do
    kubectl exec --tty deployment/grpc-testing-deployment -- /go/bin/ghz --config=./test/ghz_config.json --duration ${duration} --concurrency=${concurrency} $service > ${output}/result-${concurrency}
    sleep 0.01
  done
done
