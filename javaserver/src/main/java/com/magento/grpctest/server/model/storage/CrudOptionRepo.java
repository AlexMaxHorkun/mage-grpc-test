package com.magento.grpctest.server.model.storage;

import com.magento.grpctest.server.model.storage.data.Option;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CrudOptionRepo extends CrudRepository<Option, UUID> {
    List<Option> findByProduct_IdInOrderByProductIdAsc(Collection<UUID> productIds);
}
