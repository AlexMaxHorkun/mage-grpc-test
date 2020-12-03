package com.magento.grpctest.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@RestController
@RequestMapping("/invoke-generate")
public class GenerateTest {
    public static class TestResponse {
        private Duration phpClientTime;

        private Duration phpAsyncClientTime;

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

        public Duration getPhpAsyncClientTime() {
            return phpAsyncClientTime;
        }

        public void setPhpAsyncClientTime(Duration phpAsyncClientTime) {
            this.phpAsyncClientTime = phpAsyncClientTime;
        }
    }

    public enum Client {
        PHP,
        JAVA
    }

    private final GrpcClient phpClient;

    private final GrpcClient javaClient;

    public GenerateTest(@Autowired GrpcClient phpClient, @Autowired GrpcClient javaClient) {
        this.phpClient = phpClient;
        this.javaClient = javaClient;
    }

    @GetMapping
    public ResponseEntity<TestResponse> generate(@Valid @NotNull @Min(1) @RequestParam("count") Integer count,
                                                 @Valid @NotNull @Min(1) @RequestParam("connections") Integer connections,
                                                 @Valid @RequestParam("client") Client client) {
        if (client == null) {
            client = Client.PHP;
        }
        var resp = new TestResponse();
        if (client == Client.PHP) {
            resp.setPhpClientTime(phpClient.callGenerate(false, count, connections));
            resp.setPhpAsyncClientTime(phpClient.callGenerate(true, count, connections));
            resp.setJavaClientTime(javaClient.callGenerate(false, count, connections));
            resp.setJavaAsyncClientTime(javaClient.callGenerate(true, count, connections));
        } else {
            resp.setJavaClientTime(javaClient.callGenerate(false, count, connections));
            resp.setJavaAsyncClientTime(javaClient.callGenerate(true, count, connections));
            resp.setPhpClientTime(phpClient.callGenerate(false, count, connections));
            resp.setPhpAsyncClientTime(phpClient.callGenerate(true, count, connections));
        }

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
