package com.magento.grpctest.server.model.storage;

import com.magento.grpctest.server.model.storage.data.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CrudProductRepo extends CrudRepository<Product, UUID> {
    @Query(value = "SELECT count(p) FROM Product p")
    public long fetchCount();

    @Modifying
    @Query(value = "DELETE FROM prods", nativeQuery = true)
    public void deleteProducts();
}
