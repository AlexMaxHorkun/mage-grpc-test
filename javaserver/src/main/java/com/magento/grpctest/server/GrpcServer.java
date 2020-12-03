package com.magento.grpctest.server;

import com.magento.grpctest.def.ProductsGrpc;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final Integer port;

    private final ProductsGrpc.ProductsImplBase products;

    public GrpcServer(
            @Value("${com.magento.grpctest.server.port}") Integer port,
            @Autowired ProductsGrpc.ProductsImplBase products
    ) {
        this.port = port;
        this.products = products;
    }

    public void serve() throws Exception {
        var server = ServerBuilder.forPort(port)
                .addService(products)
                .build();

        server.start();
        server.awaitTermination();
    }
}
