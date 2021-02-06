package com.magento.grpctest.server.model.storage;

import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.magento.grpctest.def.Magegrpc;

public interface PersistResult {
    Stream<Magegrpc.Product> getResults();

    Future<Boolean> getCompleted();
}
