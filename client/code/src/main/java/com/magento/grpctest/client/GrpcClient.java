package com.magento.grpctest.client;

import com.magento.grpctest.def.ProductsGrpc;
import com.magento.grpctest.def.Test;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class GrpcClient {
    private final String uri;

    private final Integer port;

    private final class GeneratedObserver implements StreamObserver<Test.GeneratedProd> {
        private final CountDownLatch latch;

        private final Set<Test.GeneratedProd> received;

        public GeneratedObserver(CountDownLatch latch, Set<Test.GeneratedProd> received) {
            this.latch = latch;
            this.received = received;
        }

        @Override
        public void onNext(Test.GeneratedProd generatedProd) {
            received.add(generatedProd);
        }

        @Override
        public void onError(Throwable throwable) {
            latch.countDown();
            throw new RuntimeException(throwable);
        }

        @Override
        public void onCompleted() {
            latch.countDown();
        }
    }

    public GrpcClient(String uri, Integer port) {
        this.uri = uri;
        this.port = port;
    }

    public Duration callGenerate(boolean async) {
        var count = 10000;
        var channel = ManagedChannelBuilder.forAddress(uri, port).build();
        var prodService = ProductsGrpc.newStub(channel);
        try {
            var latch = new CountDownLatch(1);
            var received = new HashSet<Test.GeneratedProd>();
            var started = LocalDateTime.now();
            prodService.generate(Test.GenerateArg.newBuilder().setNumber(count).setAsync(async).build(),
                    new GeneratedObserver(latch, received));
            try {
                latch.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException("Failed to wait for the products");
            }
            var finished = LocalDateTime.now();

            //Validating response
            if (received.size() != count) {
                throw new RuntimeException("Invalid response");
            }
            for (Test.GeneratedProd prod : received) {
                if (prod.getId() == null || prod.getSku() == null || prod.getPrice() == 0.0) {
                    throw new RuntimeException("Invalid response item");
                }
            }


            return Duration.between(finished, started);
        } finally {
            channel.shutdown();
        }
    }
}
