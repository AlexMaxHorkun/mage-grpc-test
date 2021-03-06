package com.magento.grpctest.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public GrpcClient phpClient() {
        return new GrpcClient("mage-grpc-phpserver", 9000);
    }

    @Bean
    public GrpcClient javaClient() {
        return new GrpcClient("mage-grpc-javaserver", 9000);
    }

    @Bean
    public GrpcClient mageClient() {
        return new GrpcClient("catalogstorefront", 9001);
    }
}
