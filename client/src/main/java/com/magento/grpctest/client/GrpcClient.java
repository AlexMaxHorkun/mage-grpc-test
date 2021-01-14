package com.magento.grpctest.client;

import com.magento.grpctest.client.data.Measurement;
import com.magento.grpctest.def.Magegrpc;
import com.magento.grpctest.def.ProductsGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public final class GrpcClient {
    private final String uri;

    private final Integer port;

    private final static class CallResult {
        private final Set<Magegrpc.Product> products;

        private final Duration duration;

        public CallResult(Set<Magegrpc.Product> products, Duration duration) {
            this.products = products;
            this.duration = duration;
        }

        public Set<Magegrpc.Product> getProducts() {
            return products;
        }

        public Duration getDuration() {
            return duration;
        }
    }

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

        private final CompletableFuture<CallResult> future = new CompletableFuture<>();

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
                var started = LocalDateTime.now();
                var responseObserver = new GeneratedObserver();
                prodService.generate(Magegrpc.GenerateArg.newBuilder().setNumber(count).setAsync(async).build(),
                        responseObserver);
                var received = responseObserver.getFuture().get();
                future.complete(new CallResult(received, Duration.between(started, LocalDateTime.now())));
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
            } finally {
                if (channel != null) {
                    channel.shutdown();
                }
            }
        }

        public Future<CallResult> getFuture() {
            return future;
        }
    }

    private final static class ReadObserver implements StreamObserver<Magegrpc.ReadResponse> {
        private final CompletableFuture<Magegrpc.ReadResponse> future = new CompletableFuture<>();

        private Magegrpc.ReadResponse received;

        public ReadObserver() {}

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

        public Future<Magegrpc.ReadResponse> getFuture() {
            return future;
        }
    }

    private final class ReadClient implements Runnable {
        private final Integer number;

        private final CompletableFuture<CallResult> future = new CompletableFuture<>();

        public ReadClient(Integer number) {
            this.number = number;
        }

        @Override
        public void run() {
            var channel = createChannel();
            try {
                var service = ProductsGrpc.newStub(channel);
                var observer = new ReadObserver();
                var readFuture = observer.getFuture();
                var started = LocalDateTime.now();
                service.read(Magegrpc.ReadRequest.newBuilder().setN(number).build(), observer);
                try {
                    future.complete(new CallResult(new HashSet<>(readFuture.get().getItemsList()),
                            Duration.between(started, LocalDateTime.now())));
                } catch (Throwable ex) {
                    future.completeExceptionally(ex);
                }
            } finally {
                channel.shutdown();
            }
        }

        public Future<CallResult> getFuture() {
            return future;
        }
    }

    public GrpcClient(String uri, Integer port) {
        this.uri = uri;
        this.port = port;
    }

    public Measurement callGenerate(boolean async, int count, int connectionsCount) {
        var responseFutures = new HashSet<Future<CallResult>>();
        var executor = Executors.newFixedThreadPool(Math.min(connectionsCount, 16));
        var started = LocalDateTime.now();
        //Calling clients
        for (int i = 0; i < connectionsCount; i++) {
            var client = new GenerateClient(count, async);
            executor.submit(client);
            responseFutures.add(client.getFuture());
        }
        executor.shutdown();

        //Waiting for results
        var results = new HashSet<Set<Magegrpc.Product>>();
        var responseMeasurements = new LinkedList<Duration>();
        try {
            for (var future : responseFutures) {
                results.add(future.get().getProducts());
                responseMeasurements.add(future.get().getDuration());
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

        return Measurement.from(Duration.between(started, finished), responseMeasurements);
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

    public Measurement callRead(int number, int connections) {
        var responsePromises = new LinkedList<Future<CallResult>>();
        var items = new LinkedList<Magegrpc.Product>();
        var responseMeasurements = new HashSet<Duration>();
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
                items.addAll(promise.get().getProducts());
                responseMeasurements.add(promise.get().getDuration());
            } catch (Throwable ex) {
                readerException = ex;
            }
        }
        var finished = LocalDateTime.now();
        if (readerException != null) {
            throw new RuntimeException(readerException);
        }

        //Validating responses
        validateProductsReceived(items);

        return Measurement.from(Duration.between(started, finished), responseMeasurements);
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
