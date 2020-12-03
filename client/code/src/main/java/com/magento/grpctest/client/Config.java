package com.magento.grpctest.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public GrpcClient phpClient() {
        return new GrpcClient("mage-php-grpc", 9000);
    }

    @Bean
    public GrpcClient javaClient() {
        return new GrpcClient("mage-java-grpc", 9000);
    }
}
