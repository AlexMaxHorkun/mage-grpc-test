package com.magento.grpctest.server.model.storage.rdb;

import com.magento.grpctest.def.Magegrpc;
import com.magento.grpctest.server.model.storage.PersistResult;
import com.magento.grpctest.server.model.storage.ProductRepo;
import com.magento.grpctest.server.model.storage.rdb.data.Option;
import com.magento.grpctest.server.model.storage.rdb.data.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductRdbRepo implements ProductRepo {
    public final static class RdbPersistResult implements PersistResult {
        private final Stream<Product> results;

        private final Future<Boolean> completed;

        protected RdbPersistResult(Stream<Product> results, Future<Boolean> completed) {
            this.results = results;
            this.completed = completed;
        }

        @Override
        public Stream<Magegrpc.Product> getResults() {
            return results.map(ProductRdbRepo::convertToProduct);
        }

        @Override
        public Future<Boolean> getCompleted() {
            return completed;
        }
    }

    private final CrudProductRepo crudRepo;

    private final Persister persister;

    private final SecureRandom rand = new SecureRandom();

    private final CrudOptionRepo optionRepo;

    public ProductRdbRepo(@Autowired CrudProductRepo crudRepo, @Autowired CrudOptionRepo optionRepo,
                          @Autowired Persister persister) {
        this.crudRepo = crudRepo;
        this.optionRepo = optionRepo;
        this.persister = persister;
    }

    @Override
    public PersistResult persistAll(Set<Magegrpc.Product> products) {
        var futures = new HashSet<CompletableFuture<Product>>();
        for (int i = 0; i < products.size(); i++) {
            futures.add(new CompletableFuture<>());
        }
        var toComplete = new CompletableFuture<Boolean>();
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                persister.persistAll(futures, new LinkedList<>(products.stream().map(ProductRdbRepo::convertToRow)
                        .collect(Collectors.toSet())));
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

        return new RdbPersistResult(futures.stream().map(this::extractFutureResult), toComplete);
    }

    @Override
    public long count() {
        return crudRepo.fetchCount();
    }

    @Transactional
    @Override
    public void clear() {
        crudRepo.deleteProducts();
    }

    @Transactional
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

        var products = crudRepo.findAll(PageRequest.of(page, limit)).getContent()
                .stream().collect(Collectors.toMap(Product::getId, p -> p));
        products.forEach((id, p) -> p.setOptions(new HashSet<>()));
        var options = optionRepo.findByProduct_IdInOrderByProductIdAsc(products.keySet());
        for (var option : options) {
            products.get(option.getProduct().getId()).getOptions().add(option);
        }

        return products.values().stream().map(ProductRdbRepo::convertToProduct).distinct().collect(Collectors.toList());
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
            rowOption.setProduct(row);
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
}
