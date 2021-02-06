package com.magento.grpctest.server.model.storage.elastic;

import com.magento.grpctest.server.model.storage.elastic.data.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface ElasticProductRepo extends ElasticsearchRepository<Product, UUID> {
}
