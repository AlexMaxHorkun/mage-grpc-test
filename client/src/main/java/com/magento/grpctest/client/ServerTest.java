package com.magento.grpctest.client;

import com.magento.grpctest.client.data.Measurement;
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
        private Measurement phpClientTime;

        private Measurement phpAsyncClientTime;

        private Measurement javaClientTime;

        private Measurement javaAsyncClientTime;

        private Measurement mageClientTime;

        private Measurement mageAsyncClientTime;

        public GenerateResponse() { }

        public Measurement getPhpClientTime() {
            return phpClientTime;
        }

        public void setPhpClientTime(Measurement phpClientTime) {
            this.phpClientTime = phpClientTime;
        }

        public Measurement getJavaClientTime() {
            return javaClientTime;
        }

        public void setJavaClientTime(Measurement javaClientTime) {
            this.javaClientTime = javaClientTime;
        }

        public Measurement getJavaAsyncClientTime() {
            return javaAsyncClientTime;
        }

        public void setJavaAsyncClientTime(Measurement javaAsyncClientTime) {
            this.javaAsyncClientTime = javaAsyncClientTime;
        }

        public Measurement getMageClientTime() {
            return mageClientTime;
        }

        public void setMageClientTime(Measurement mageClientTime) {
            this.mageClientTime = mageClientTime;
        }

        public Measurement getMageAsyncClientTime() {
            return mageAsyncClientTime;
        }

        public void setMageAsyncClientTime(Measurement mageAsyncClientTime) {
            this.mageAsyncClientTime = mageAsyncClientTime;
        }

        public Measurement getPhpAsyncClientTime() {
            return phpAsyncClientTime;
        }

        public void setPhpAsyncClientTime(Measurement phpAsyncClientTime) {
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
        private Measurement phpReadTime;

        private Measurement javaReadTime;

        private Measurement mageReadTime;

        public ReadResponse() {}

        public ReadResponse(Measurement phpReadTime, Measurement javaReadTime, Measurement mageReadTime) {
            this.phpReadTime = phpReadTime;
            this.javaReadTime = javaReadTime;
            this.mageReadTime = mageReadTime;
        }

        public Measurement getPhpReadTime() {
            return phpReadTime;
        }

        public Measurement getJavaReadTime() {
            return javaReadTime;
        }

        public Measurement getMageReadTime() {
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
        Measurement phpReadResult = null;
        Measurement javaReadResult = null;
        Measurement mageReadResult = null;
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
