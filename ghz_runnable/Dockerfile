# based on https://github.com/obvionaoe/ghz-docker
FROM mslabko/grpc-testing-ghz

COPY ghz_config.json /test/ghz_config.json
# copy proto from ../grpc/src/main/proto/magegrpc.proto first
COPY magegrpc.proto /test/test.proto

ENTRYPOINT ["./entrypoint.sh"]
