package com.epam.springCoreTask.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class StorageFileHealthIndicatorTest {

    private final StorageFileHealthIndicator indicator = new StorageFileHealthIndicator();

    @Test
    void healthIsUpWhenStorageFileExists() throws Exception {
        // storage-init.txt is present in test classpath (copied from main resources)
        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("file");
        assertThat(health.getDetails().get("status")).isEqualTo("readable");
    }
}
