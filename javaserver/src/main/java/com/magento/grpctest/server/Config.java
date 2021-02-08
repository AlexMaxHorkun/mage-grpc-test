package com.magento.grpctest.server;

import com.magento.grpctest.server.model.storage.ProductRepo;
import com.magento.grpctest.server.model.storage.elastic.ProductElasticRepo;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetSocketAddress;

@Configuration
@EnableElasticsearchRepositories
public class Config {
    @Bean
    public RestHighLevelClient client(@Value("${com.magento.grpctest.elastic-host}") String host,
                                      @Value("${com.magento.grpctest.elastic-port}") String port) {
        ClientConfiguration clientConfiguration
                = ClientConfiguration.builder()
                .connectedTo(new InetSocketAddress(host, Integer.parseInt(port)))
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(@Value("${com.magento.grpctest.elastic-host}") String host,
                                                         @Value("${com.magento.grpctest.elastic-port}") String port) {
        return new ElasticsearchRestTemplate(client(host, port));
    }

    @Bean
    public ProductRepo repo(@Autowired ProductElasticRepo productElasticRepo) {
        return productElasticRepo;
    }
}
