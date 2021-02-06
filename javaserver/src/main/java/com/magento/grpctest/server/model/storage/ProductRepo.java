package com.magento.grpctest.server.model.storage;

import com.magento.grpctest.def.Magegrpc;

import java.util.List;
import java.util.Set;

public interface ProductRepo {

    PersistResult persistAll(Set<Magegrpc.Product> products);

    long count();

    void clear();

    List<Magegrpc.Product> findAll(int limit);
}
