FROM fullstorydev/grpcui:v1.0.0
COPY proto /etc/magento/proto
CMD /bin/grpcui -bind 0.0.0.0 -port 8080 -plaintext -import-path /etc/magento/proto -proto magegrpc.proto mage-grpc-phpserver:9000