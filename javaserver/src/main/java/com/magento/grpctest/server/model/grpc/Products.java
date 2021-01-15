package com.magento.grpctest.server.model.grpc;

import com.magento.grpctest.def.Magegrpc;
import com.magento.grpctest.def.ProductsGrpc;
import com.magento.grpctest.server.model.ProductManager;
import com.magento.grpctest.server.model.storage.ProductRepo;
import com.magento.grpctest.server.model.storage.data.Product;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class Products extends ProductsGrpc.ProductsImplBase {
    private static final int[] BatchSizes = {5000, 2000, 1000, 500, 250, 100};

    private final static class PropagationResult {
        private final Throwable error;

        public PropagationResult(Throwable error) {
            this.error = error;
        }

        public PropagationResult() {
            this.error = null;
        }

        public Throwable getError() {
            return error;
        }

        public boolean wasSuccessful() {
            return error == null;
        }
    }

    private final static class StreamWrapper {
        private final StreamObserver<Magegrpc.Product> observer;

        public StreamWrapper(StreamObserver<Magegrpc.Product> observer) {
            this.observer = observer;
        }

        public synchronized void send(Magegrpc.Product message) {
            observer.onNext(message);
        }
    }

    private final static class GeneratedResultPropagator implements Runnable {
        private final ProductRepo.PersistResult persisted;

        private final StreamWrapper responseObserver;

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final CompletableFuture<PropagationResult> future = new CompletableFuture<>();

        public GeneratedResultPropagator(ProductRepo.PersistResult persisted, StreamWrapper responseObserver) {
            this.persisted = persisted;
            this.responseObserver = responseObserver;
        }

        @Override
        public void run() {
            try {
                persisted.getResults().forEach(this::relay);
                //Waiting for persist operation to finish
                persisted.getCompleted().get();
            } catch (Throwable ex) {
                logger.error(ex.getMessage());
                future.complete(new PropagationResult(ex));
            }
            future.complete(new PropagationResult());
        }

        public Future<PropagationResult> getFuture() {
            return future;
        }

        private void relay(Product product) {
            responseObserver.send(createProductOf(product));
        }
    }

    private final ProductManager manager;

    private final Logger logger = LoggerFactory.getLogger(Products.class);

    public Products(@Autowired ProductManager manager) {
        this.manager = manager;
    }

    @Override
    public void generate(Magegrpc.GenerateArg request, StreamObserver<Magegrpc.Product> responseObserver) {
        try {
            var streamWrapper = new StreamWrapper(responseObserver);
            var numberRequested = request.getNumber();
            var batchSize = numberRequested;
            var batchCount = 1;
            if (request.getAsync()) {
                //Trying to have at least 4 batches if possible
                for (var size : BatchSizes) {
                    if (size < numberRequested) {
                        batchSize = size;
                        if (size * 4 <= numberRequested) {
                            break;
                        }
                    }
                }
                batchCount = numberRequested / batchSize + ((numberRequested % batchSize == 0) ? 0 : 1);
            }

            var executor = Executors.newFixedThreadPool(4);
            var relaysCompleted = new HashSet<Future<PropagationResult>>();
            for (int batch = 1; batch <= batchCount; batch++) {
                var count = batchSize;
                if (batch * batchSize > numberRequested) {
                    count = numberRequested - (batch - 1) * batchSize;
                }

                logger.warn("[GRPC] Generating products...");
                var relay = new GeneratedResultPropagator(manager.generate(count), streamWrapper);
                relaysCompleted.add(relay.getFuture());
                executor.submit(relay);
            }
            executor.shutdown();

            //Waiting for all streams to finish
            Throwable relayError = null;
            for (var relayCompleted : relaysCompleted) {
                if (!relayCompleted.get().wasSuccessful()) {
                    //Don't stop on an error because we need all product streams to finish before throwing an error.
                    relayError = relayCompleted.get().getError();
                }
            }
            if (relayError != null) {
                //One of the propagators had an error
                throw relayError;
            }

            responseObserver.onCompleted();
        } catch (Throwable ex) {
            logger.error(ex.getMessage());
            responseObserver.onError(ex);
            throw new RuntimeException(ex);
        } finally {
            logger.warn(String.format("[GRPC] Total %d products exist", manager.count()));
        }
    }

    @Override
    public void clear(Magegrpc.ClearArg request, StreamObserver<Magegrpc.Cleared> responseObserver) {
        try {
            manager.clear();
            responseObserver.onNext(Magegrpc.Cleared.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Throwable ex) {
            responseObserver.onError(ex);
            logger.error(ex.getMessage());
        }
    }

    @Override
    public void read(Magegrpc.ReadRequest request, StreamObserver<Magegrpc.ReadResponse> responseObserver) {
        var response = Magegrpc.ReadResponse.newBuilder();
        try {
            manager.find(request.getN()).forEach(p -> response.addItems(createProductOf(p)));
        } catch (Throwable ex) {
            responseObserver.onError(ex);
            logger.error("[GRPC] Read operation error", ex);
            return;
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private static Magegrpc.Product createProductOf(Product product) {
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
