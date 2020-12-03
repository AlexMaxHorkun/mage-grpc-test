package com.magento.grpctest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clear")
public class Clear {
    private final GrpcClient phpClient;

    private final GrpcClient javaClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    public Clear(@Autowired GrpcClient phpClient, @Autowired GrpcClient javaClient) {
        this.phpClient = phpClient;
        this.javaClient = javaClient;
    }

    @PostMapping
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
}
