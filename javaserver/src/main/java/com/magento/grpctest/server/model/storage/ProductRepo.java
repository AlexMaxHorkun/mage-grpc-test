package com.magento.grpctest.server.model.storage;

import com.magento.grpctest.server.model.storage.data.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    public ProductRepo(@Autowired CrudProductRepo crudRepo, @Autowired Persister persister) {
        this.crudRepo = crudRepo;
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
