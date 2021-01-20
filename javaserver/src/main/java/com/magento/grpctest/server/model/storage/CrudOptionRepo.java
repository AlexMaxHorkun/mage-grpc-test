package com.magento.grpctest.server.model.storage;

import com.magento.grpctest.server.model.storage.data.Option;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CrudOptionRepo extends CrudRepository<Option, UUID> {
    @Query(value = "SELECT o FROM Option o WHERE o.product.id in (:ids) ORDER BY o.product.id")
    List<Option> findByProductIds(@Param("ids") Collection<UUID> productIds);
}
