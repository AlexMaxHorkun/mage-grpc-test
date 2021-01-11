package com.magento.grpctest.server.model;

import com.magento.grpctest.server.model.storage.ProductRepo;
import com.magento.grpctest.server.model.storage.data.Option;
import com.magento.grpctest.server.model.storage.data.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class ProductManager {
    private final ProductRepo repo;

    private final SecureRandom rand = new SecureRandom();

    public ProductManager(@Autowired ProductRepo repo) {
        this.repo = repo;
    }

    public ProductRepo.PersistResult generate(int count) {
        var generated = new HashSet<Product>();
        for (int i = 0; i < count; i++) {
            var id = 1 +rand.nextInt(100000);
            var prod = new Product();
            prod.setId(UUID.randomUUID());
            prod.setSku(String.format("sku_%d", id));
            prod.setTitle(String.format("Product #%d", id));
            prod.setDescription(String.format("Generated Product #%d", id));
            prod.setPrice(rand.nextFloat() * 10000);
            prod.setImgUrl(String.format("/media/prod_img_%d.jpg", id));
            prod.setAvailable(true);
            var options = new HashSet<Option>();
            for (int j = 0; j < 10; j++) {
                var option = new Option();
                option.setId(UUID.randomUUID());
                option.setTitle(String.format("Option #%d", j));
                option.setPrice(rand.nextFloat() * 10000);
                option.setAvailable(true);
                options.add(option);
                option.setProduct(prod);
            }
            prod.setOptions(options);

            generated.add(prod);
        }

        return repo.persistAll(generated);
    }

    public long count() {
        return repo.count();
    }

    public void clear() {
        repo.clear();
    }

    public List<Product> find(int limit) {
        return repo.findAll(limit);
    }
}
