package com.epam.springCoreTask.monitoring;

import javax.sql.DataSource;
import java.sql.Connection;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("database")
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator extends AbstractHealthIndicator {

    private final DataSource dataSource;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        log.debug("Checking database health");
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (valid) {
                builder.up()
                        .withDetail("database", connection.getMetaData().getDatabaseProductName())
                        .withDetail("url", connection.getMetaData().getURL());
                log.debug("Database health check passed");
            } else {
                builder.down().withDetail("reason", "Connection is not valid");
                log.warn("Database health check failed: connection invalid");
            }
        }
    }
}
