package com.magento.grpctest.server.model;

import com.magento.grpctest.def.Magegrpc;
import com.magento.grpctest.server.model.storage.PersistResult;
import com.magento.grpctest.server.model.storage.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductManager {
    private final ProductRepo repo;

    private final SecureRandom rand = new SecureRandom();

    public ProductManager(@Autowired ProductRepo repo) {
        this.repo = repo;
    }

    public Set<Magegrpc.Product> generateData(int count) {
        var generated = new HashSet<Magegrpc.Product>();
        for (int i = 0; i < count; i++) {
            var id = 1 +rand.nextInt(100000);
            var prod = Magegrpc.Product.newBuilder();
            prod.setId(UUID.randomUUID().toString());
            prod.setSku(String.format("sku_%d", id));
            prod.setTitle(String.format("Product #%d", id));
            prod.setDescription(String.format("Generated Product #%d", id));
            prod.setPrice(rand.nextFloat() * 10000);
            prod.setImgUrl(String.format("/media/prod_img_%d.jpg", id));
            prod.setAvailable(true);
            var options = new HashSet<Magegrpc.Option>();
            for (int j = 0; j < 10; j++) {
                var option = Magegrpc.Option.newBuilder();
                option.setId(UUID.randomUUID().toString());
                option.setTitle(String.format("Option #%d", j));
                option.setPrice(rand.nextFloat() * 10000);
                option.setAvailable(true);
                options.add(option.build());
            }
            prod.addAllOptions(options);

            generated.add(prod.build());
        }

        return generated;
    }

    public PersistResult generate(int count) {
        return repo.persistAll(generateData(count));
    }

    public long count() {
        return repo.count();
    }

    public void clear() {
        repo.clear();
    }

    public List<Magegrpc.Product> find(int limit) {
        return repo.findAll(limit);
    }
}
