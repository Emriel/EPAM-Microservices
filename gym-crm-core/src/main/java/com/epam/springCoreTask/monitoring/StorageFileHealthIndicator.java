package com.epam.springCoreTask.monitoring;

import java.io.InputStream;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component("storageFile")
@Slf4j
public class StorageFileHealthIndicator extends AbstractHealthIndicator {

    private static final String STORAGE_FILE = "storage-init.txt";

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        log.debug("Checking storage file health: {}", STORAGE_FILE);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(STORAGE_FILE)) {
            if (stream != null && stream.available() > 0) {
                builder.up().withDetail("file", STORAGE_FILE).withDetail("status", "readable");
                log.debug("Storage file health check passed: {}", STORAGE_FILE);
            } else {
                builder.down().withDetail("file", STORAGE_FILE).withDetail("reason", "File not found or empty");
                log.warn("Storage file health check failed: {}", STORAGE_FILE);
            }
        }
    }
}
