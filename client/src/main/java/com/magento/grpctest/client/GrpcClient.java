package com.magento.grpctest.client;

import com.magento.grpctest.def.Magegrpc;
import com.magento.grpctest.def.ProductsGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.*;

public final class GrpcClient {
    private final String uri;

    private final Integer port;

    private final static class GeneratedObserver implements StreamObserver<Magegrpc.Product> {
        private final CompletableFuture<Set<Magegrpc.Product>> future = new CompletableFuture<>();

        private final Set<Magegrpc.Product> received = new HashSet<>();

        public GeneratedObserver() { }

        @Override
        public void onNext(Magegrpc.Product generatedProd) {
            received.add(generatedProd);
        }

        @Override
        public void onError(Throwable throwable) {
            future.completeExceptionally(throwable);
        }

        @Override
        public void onCompleted() {
            future.complete(received);
        }

        public Future<Set<Magegrpc.Product>> getFuture() {
            return future;
        }
    }

    private final class GenerateClient implements Runnable {
        private final int count;

        private final boolean async;

        private final CompletableFuture<Set<Magegrpc.Product>> future = new CompletableFuture<>();

        public GenerateClient(int count, boolean async) {
            this.count = count;
            this.async = async;
        }

        @Override
        public void run() {
            ManagedChannel channel = null;
            try {
                channel = createChannel();
                var prodService = ProductsGrpc.newStub(channel);
                var responseObserver = new GeneratedObserver();
                prodService.generate(Magegrpc.GenerateArg.newBuilder().setNumber(count).setAsync(async).build(),
                        responseObserver);
                future.complete(responseObserver.getFuture().get());
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
            } finally {
                if (channel != null) {
                    channel.shutdown();
                }
            }
        }

        public Future<Set<Magegrpc.Product>> getFuture() {
            return future;
        }
    }

    private final static class ReadObserver implements StreamObserver<Magegrpc.ReadResponse> {
        private final CompletableFuture<Magegrpc.ReadResponse> future;

        private Magegrpc.ReadResponse received;

        public ReadObserver(CompletableFuture<Magegrpc.ReadResponse> future) {
            this.future = future;
        }

        @Override
        public void onNext(Magegrpc.ReadResponse readResponse) {
            received = readResponse;
        }

        @Override
        public void onError(Throwable throwable) {
            future.completeExceptionally(throwable);
        }

        @Override
        public void onCompleted() {
            future.complete(received);
        }
    }

    private final class ReadClient implements Runnable {
        private final Integer number;

        private final CompletableFuture<Magegrpc.ReadResponse> future = new CompletableFuture<>();

        public ReadClient(Integer number) {
            this.number = number;
        }

        @Override
        public void run() {
            var channel = createChannel();
            try {
                var service = ProductsGrpc.newStub(channel);
                service.read(Magegrpc.ReadRequest.newBuilder().setN(number).build(), new ReadObserver(future));
                try {
                    future.get();
                } catch (Throwable ex) {
                    //Ignore, let the main class decide.
                }
            } finally {
                channel.shutdown();
            }
        }

        public Future<Magegrpc.ReadResponse> getFuture() {
            return future;
        }
    }

    public GrpcClient(String uri, Integer port) {
        this.uri = uri;
        this.port = port;
    }

    public Duration callGenerate(boolean async, int count, int connectionsCount) {
        var callResults = new HashSet<Future<Set<Magegrpc.Product>>>();
        var executor = Executors.newFixedThreadPool(Math.min(connectionsCount, 16));
        var started = LocalDateTime.now();
        //Calling clients
        for (int i = 0; i < connectionsCount; i++) {
            var client = new GenerateClient(count, async);
            executor.submit(client);
            callResults.add(client.getFuture());
        }
        executor.shutdown();

        //Waiting for results
        var results = new HashSet<Set<Magegrpc.Product>>();
        try {
            for (var future : callResults) {
                results.add(future.get());
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException("Failed to wait for threads", ex);
        } catch (CancellationException ex) {
            throw new RuntimeException("Canceled somehow", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
        var finished = LocalDateTime.now();

        //Validating results
        for (var result : results) {
            if (result.size() != count) {
                throw new RuntimeException("Not all products generated by the GRPC server");
            }
            validateProductsReceived(result);
        }

        return Duration.between(started, finished);
    }

    public void clear() {
        var channel = createChannel();
        try {
            var products = ProductsGrpc.newBlockingStub(channel);
            products.clear(Magegrpc.ClearArg.newBuilder().build());
        } finally {
            channel.shutdown();
        }
    }

    public Duration callRead(int number, int connections) {
        var responsePromises = new LinkedList<Future<Magegrpc.ReadResponse>>();
        var responses = new LinkedList<Magegrpc.Product>();
        var executor = Executors.newFixedThreadPool(16);
        var started = LocalDateTime.now();

        for (int i = 0; i < connections; i++) {
            var reader = new ReadClient(number);
            responsePromises.add(reader.getFuture());
            executor.submit(reader);
        }
        executor.shutdown();

        //Waiting for responses
        Throwable readerException = null;
        for (var promise : responsePromises) {
            try {
                responses.addAll(promise.get().getItemsList());
            } catch (Throwable ex) {
                readerException = ex;
            }
        }
        var finished = LocalDateTime.now();
        if (readerException != null) {
            throw new RuntimeException(readerException);
        }

        //Validating responses
        validateProductsReceived(responses);

        return Duration.between(started, finished);
    }

    private ManagedChannel createChannel() {
        return ManagedChannelBuilder.forAddress(uri, port).usePlaintext().build();
    }

    private void validateProductsReceived(Iterable<Magegrpc.Product> products) {
        for (var prod : products) {
            if (prod.getId() == null || prod.getSku() == null || prod.getPrice() == 0.0 || prod.getId().equals("")
                    || prod.getOptionsCount() == 0) {
                throw new RuntimeException("Invalid Product item received");
            }
            for (var option : prod.getOptionsList()) {
                if (option.getId() == null || option.getId().equals("")) {
                    throw new RuntimeException("Invalid Product item received");
                }
            }
        }
    }
}
