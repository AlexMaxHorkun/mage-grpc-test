package com.magento.grpctest.server.model.storage.elastic;

import com.magento.grpctest.def.Magegrpc;
import com.magento.grpctest.server.model.storage.PersistResult;
import com.magento.grpctest.server.model.storage.ProductRepo;
import com.magento.grpctest.server.model.storage.elastic.data.Option;
import com.magento.grpctest.server.model.storage.elastic.data.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductElasticRepo implements ProductRepo {
    private final static class ElasticPersistResult implements PersistResult {
        private Stream<Magegrpc.Product> products;

        private Future<Boolean> completed;

        public ElasticPersistResult(Stream<Magegrpc.Product> products, Future<Boolean> completed) {
            this.products = products;
            this.completed = completed;
        }

        @Override
        public Stream<Magegrpc.Product> getResults() {
            return products;
        }

        @Override
        public Future<Boolean> getCompleted() {
            return completed;
        }
    }

    private final ElasticProductRepo repo;

    private final SecureRandom rand = new SecureRandom();

    public ProductElasticRepo(@Autowired ElasticProductRepo repo) {
        this.repo = repo;
    }

    @Override
    public PersistResult persistAll(Set<Magegrpc.Product> products) {
        var futures = new HashSet<CompletableFuture<Magegrpc.Product>>();
        for (int i = 0; i < products.size(); i++) {
            futures.add(new CompletableFuture<>());
        }
        var toComplete = new CompletableFuture<Boolean>();
        var toPersist = products.stream().map(ProductElasticRepo::convertToRow).distinct()
                .collect(Collectors.toCollection(LinkedList::new));
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                for (var future : futures) {
                    try {
                        var saved = repo.save(toPersist.remove());
                        future.complete(convertToProduct(saved));
                    } catch (Throwable ex) {
                        future.completeExceptionally(ex);
                        throw ex;
                    }
                }
                toComplete.complete(true);
            } catch (Throwable ex) {
                for (var future : futures) {
                    if (!future.isDone()) {
                        future.completeExceptionally(ex);
                    }
                }
                toComplete.completeExceptionally(ex);
            }
        });
        executor.shutdown();

        return new ElasticPersistResult(futures.stream().map(this::extractFutureResult), toComplete);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public void clear() {
        repo.deleteAll();
    }

    @Override
    public List<Magegrpc.Product> findAll(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Invalid limit provided");
        }

        var count = (int)count();
        int page;
        if (count <= limit) {
            page = 1;
        } else {
            page = rand.nextInt(count / limit + ((count % limit == 0) ? 0 : 1));
        }

        return repo.findAll(PageRequest.of(page, limit)).map(ProductElasticRepo::convertToProduct).toList();
    }

    private static Product convertToRow(Magegrpc.Product product) {
        var row = new Product();
        row.setId(UUID.fromString(product.getId()));
        row.setAvailable(product.getAvailable());
        row.setPrice(product.getPrice());
        row.setDescription(product.getDescription());
        row.setSku(product.getSku());
        row.setTitle(product.getTitle());
        row.setImgUrl(product.getImgUrl());

        var rowOptions = new HashSet<Option>();
        for (var opt : product.getOptionsList()) {
            var rowOption = new Option();
            rowOption.setId(UUID.fromString(opt.getId()));
            rowOption.setAvailable(opt.getAvailable());
            rowOption.setPrice(opt.getPrice());
            rowOption.setTitle(opt.getTitle());
            rowOptions.add(rowOption);
        }
        row.setOptions(rowOptions);

        return row;
    }

    private static Magegrpc.Product convertToProduct(Product product) {
        var grpcProduct = Magegrpc.Product.newBuilder()
                .setId(product.getId().toString())
                .setSku(product.getSku())
                .setTitle(product.getTitle())
                .setDescription(product.getDescription())
                .setPrice(product.getPrice())
                .setAvailable(product.getAvailable())
                .setImgUrl(product.getImgUrl());
        for (var option : product.getOptions()) {
            grpcProduct.addOptions(Magegrpc.Option.newBuilder()
                    .setId(option.getId().toString())
                    .setTitle(option.getTitle())
                    .setPrice(option.getPrice())
                    .setAvailable(option.getAvailable())
                    .build());
        }

        return grpcProduct.build();
    }

    private <T> T extractFutureResult(Future<T> future) {
        try {
            return future.get();
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
