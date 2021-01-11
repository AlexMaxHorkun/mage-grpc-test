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

        private Duration mageClientTime;

        private Duration mageAsyncClientTime;

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

        public Duration getMageClientTime() {
            return mageClientTime;
        }

        public void setMageClientTime(Duration mageClientTime) {
            this.mageClientTime = mageClientTime;
        }

        public Duration getMageAsyncClientTime() {
            return mageAsyncClientTime;
        }

        public void setMageAsyncClientTime(Duration mageAsyncClientTime) {
            this.mageAsyncClientTime = mageAsyncClientTime;
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

        private Boolean mageCleared;

        public ClearResponse(boolean javaCleared, boolean phpCleared, boolean mageCleared) {
            this.javaCleared = javaCleared;
            this.phpCleared = phpCleared;
            this.mageCleared = mageCleared;
        }

        public Boolean getJavaCleared() {
            return javaCleared;
        }

        public Boolean getPhpCleared() {
            return phpCleared;
        }

        public Boolean getMageCleared() {
            return mageCleared;
        }
    }

    public static class ReadResponse {
        private Duration phpReadTime;

        private Duration javaReadTime;

        private Duration mageReadTime;

        public ReadResponse() {}

        public ReadResponse(Duration phpReadTime, Duration javaReadTime, Duration mageReadTime) {
            this.phpReadTime = phpReadTime;
            this.javaReadTime = javaReadTime;
            this.mageReadTime = mageReadTime;
        }

        public Duration getPhpReadTime() {
            return phpReadTime;
        }

        public Duration getJavaReadTime() {
            return javaReadTime;
        }

        public Duration getMageReadTime() {
            return mageReadTime;
        }
    }

    public enum Client {
        PHP,
        JAVA,
        MAGENTO
    }

    private final GrpcClient phpClient;

    private final GrpcClient javaClient;

    private final GrpcClient mageClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ServerTest(@Autowired GrpcClient phpClient, @Autowired GrpcClient javaClient,
                      @Autowired GrpcClient mageClient) {
        this.phpClient = phpClient;
        this.javaClient = javaClient;
        this.mageClient = mageClient;
    }

    @GetMapping(value = "invoke-generate")
    public ResponseEntity<GenerateResponse> generate(@Valid @NotNull @Min(1) @RequestParam("count") Integer count,
                                                 @Valid @NotNull @Min(1) @RequestParam("connections") Integer connections) {
        var resp = new GenerateResponse();
        try {
            resp.setPhpClientTime(phpClient.callGenerate(false, count, connections));
        } catch (Throwable ex) { /* Ignore */ }
        try {
            resp.setPhpAsyncClientTime(phpClient.callGenerate(true, count, connections));
        } catch (Throwable ex) { /* Ignore */ }
        try {
            resp.setJavaClientTime(javaClient.callGenerate(false, count, connections));
        } catch (Throwable ex) { /* Ignore */ }
        try {
            resp.setJavaAsyncClientTime(javaClient.callGenerate(true, count, connections));
        } catch (Throwable ex) { /* Ignore */ }
        try {
            resp.setMageClientTime(mageClient.callGenerate(false, count, connections));
        } catch (Throwable ex) { /* Ignore */ }
        try {
            resp.setMageAsyncClientTime(mageClient.callGenerate(true, count, connections));
        } catch (Throwable ex) { /* Ignore */ }

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @PostMapping(value = "clear")
    public ResponseEntity<ClearResponse> clear() {
        boolean javaCleared = true;
        boolean phpCleared = true;
        boolean mageCleared = true;
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
        try {
            mageClient.clear();
        } catch (Throwable ex) {
            mageCleared = false;
            logger.error(String.format("Error while clearing Magento server: %s", ex.getMessage()));
        }

        return new ResponseEntity<>(new ClearResponse(javaCleared, phpCleared, mageCleared), HttpStatus.OK);
    }

    @GetMapping(value = "invoke-read")
    public ResponseEntity<ReadResponse> read(@Valid @NotNull @Min(1) @RequestParam("number") Integer number,
                                             @Valid @NotNull @Min(1) @RequestParam("connections") Integer connections) {
        Duration phpReadResult = null;
        Duration javaReadResult = null;
        Duration mageReadResult = null;
        try {
            phpReadResult = phpClient.callRead(number, connections);
        } catch (Throwable ex) {
            logException(ex, Client.PHP, "read");
        }
        try {
            javaReadResult = javaClient.callRead(number, connections);
        } catch (Throwable ex) {
            logException(ex, Client.JAVA, "read");
        }
        try {
            mageReadResult = mageClient.callRead(number, connections);
        } catch (Throwable ex) {
            logException(ex, Client.MAGENTO, "read");
        }

        return new ResponseEntity<>(new ReadResponse(phpReadResult, javaReadResult, mageReadResult), HttpStatus.OK);
    }

    private void logException(Throwable exception, Client fromClient, String operationName) {
        logger.error(String.format("[GRPC TEST] Exception occurred while calling operation \"%s\" of %s client",
                operationName, fromClient),
                exception);
    }
}
