package com.magento.grpctest.server.model.storage.rdb;

import com.magento.grpctest.server.model.storage.rdb.data.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
public class Persister {
    private final CrudProductRepo repo;

    public Persister(@Autowired CrudProductRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public void persistAll(Set<CompletableFuture<Product>> toComplete, Queue<Product> products) {
        for (var future : toComplete) {
            try {
                var saved = repo.save(products.remove());
                future.complete(saved);
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
                throw ex;
            }
        }
    }
}
