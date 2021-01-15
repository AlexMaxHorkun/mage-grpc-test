package com.magento.grpctest.server.model.storage;

import com.magento.grpctest.server.model.storage.data.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductRepo {
    public final static class PersistResult {
        private final Stream<Product> results;

        private final Future<Boolean> completed;

        protected PersistResult(Stream<Product> results, Future<Boolean> completed) {
            this.results = results;
            this.completed = completed;
        }

        public Stream<Product> getResults() {
            return results;
        }

        public Future<Boolean> getCompleted() {
            return completed;
        }
    }

    private final CrudProductRepo crudRepo;

    private final Persister persister;

    private final SecureRandom rand = new SecureRandom();

    private final CrudOptionRepo optionRepo;

    public ProductRepo(@Autowired CrudProductRepo crudRepo, @Autowired CrudOptionRepo optionRepo,
                       @Autowired Persister persister) {
        this.crudRepo = crudRepo;
        this.optionRepo = optionRepo;
        this.persister = persister;
    }

    public PersistResult persistAll(Set<Product> products) {
        var futures = new HashSet<CompletableFuture<Product>>();
        for (int i = 0; i < products.size(); i++) {
            futures.add(new CompletableFuture<>());
        }
        var toComplete = new CompletableFuture<Boolean>();
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                persister.persistAll(futures, new LinkedList<>(products));
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

        return new PersistResult(futures.stream().map(this::extractFutureResult), toComplete);
    }

    public long count() {
        return crudRepo.fetchCount();
    }

    @Transactional
    public void clear() {
        crudRepo.deleteProducts();
    }

    public List<Product> findAll(int limit) {
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

        return new ArrayList<>(products.values());
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
