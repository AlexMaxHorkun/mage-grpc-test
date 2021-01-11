package com.magento.grpctest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@RestController
@RequestMapping("/")
public class ServerTest {
    public static class GenerateResponse {
        private Duration phpClientTime;

        private Duration phpAsyncClientTime;

        private Duration javaClientTime;

        private Duration javaAsyncClientTime;

        public GenerateResponse() { }

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

    public static class ClearResponse {
        private Boolean javaCleared;

        private Boolean phpCleared;

        public ClearResponse(boolean javaCleared, boolean phpCleared) {
            this.javaCleared = javaCleared;
            this.phpCleared = phpCleared;
        }

        public Boolean getJavaCleared() {
            return javaCleared;
        }

        public Boolean getPhpCleared() {
            return phpCleared;
        }
    }

    public static class ReadResponse {
        private Duration phpReadTime;

        private Duration javaReadTime;

        public ReadResponse() {}

        public ReadResponse(Duration phpReadTime, Duration javaReadTime) {
            this.phpReadTime = phpReadTime;
            this.javaReadTime = javaReadTime;
        }

        public Duration getPhpReadTime() {
            return phpReadTime;
        }

        public Duration getJavaReadTime() {
            return javaReadTime;
        }
    }

    public enum Client {
        PHP,
        JAVA
    }

    private final GrpcClient phpClient;

    private final GrpcClient javaClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ServerTest(@Autowired GrpcClient phpClient, @Autowired GrpcClient javaClient) {
        this.phpClient = phpClient;
        this.javaClient = javaClient;
    }

    @GetMapping(value = "invoke-generate")
    public ResponseEntity<GenerateResponse> generate(@Valid @NotNull @Min(1) @RequestParam("count") Integer count,
                                                 @Valid @NotNull @Min(1) @RequestParam("connections") Integer connections,
                                                 @Valid @RequestParam("client") Client client) {
        if (client == null) {
            client = Client.PHP;
        }
        var resp = new GenerateResponse();
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

    @PostMapping(value = "clear")
    public ResponseEntity<ClearResponse> clear() {
        boolean javaCleared = true;
        boolean phpCleared = true;
        try {
            javaClient.clear();
        } catch (Throwable ex) {
            javaCleared = false;
            logger.error(String.format("Error while clearing JAVA server: %s", ex.getMessage()));
        }
        try {
            phpClient.clear();
        } catch (Throwable ex) {
            phpCleared = false;
            logger.error(String.format("Error while clearing PHP server: %s", ex.getMessage()));
        }

        return new ResponseEntity<>(new ClearResponse(javaCleared, phpCleared), HttpStatus.OK);
    }

    @GetMapping(value = "invoke-read")
    public ResponseEntity<ReadResponse> read(@Valid @NotNull @Min(1) @RequestParam("number") Integer number,
                                             @Valid @NotNull @Min(1) @RequestParam("connections") Integer connections) {

        return new ResponseEntity<>(
                new ReadResponse(phpClient.callRead(number, connections), javaClient.callRead(number, connections)),
                HttpStatus.OK
        );
    }
}
