package com.magento.grpctest.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/invoke-generate")
public class GenerateTest {
    private final GrpcClient phpClient;

    private final GrpcClient javaClient;

    public class TestResponse {
        private Duration phpClientTime;

        private Duration javaClientTime;

        private Duration javaAsyncClientTime;

        public TestResponse() { }

        public Duration getPhpClientTime() {
            return phpClientTime;
        }

        public void setPhpClientTime(Duration phpClientTime) {
            this.phpClientTime = phpClientTime;
        }

        public Duration getJavaClientTime() {
            return javaClientTime;
        }

        public void setJavaClientTime(Duration javaClientTime) {
            this.javaClientTime = javaClientTime;
        }

        public Duration getJavaAsyncClientTime() {
            return javaAsyncClientTime;
        }

        public void setJavaAsyncClientTime(Duration javaAsyncClientTime) {
            this.javaAsyncClientTime = javaAsyncClientTime;
        }
    }

    public GenerateTest(@Autowired GrpcClient phpClient, @Autowired GrpcClient javaClient) {
        this.phpClient = phpClient;
        this.javaClient = javaClient;
    }

    @GetMapping
    public ResponseEntity<TestResponse> generate() {
        var resp = new TestResponse();
        resp.setPhpClientTime(phpClient.callGenerate(false));
        resp.setJavaClientTime(javaClient.callGenerate(false));
        resp.setJavaAsyncClientTime(javaClient.callGenerate(true));

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
