package com.epam.springCoreTask.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GymMetricsServiceTest {

    private SimpleMeterRegistry registry;
    private GymMetricsService metricsService;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metricsService = new GymMetricsService(registry);
        metricsService.initMetrics();
    }

    @Test
    void incrementTraineeRegistrationsIncreasesCounter() {
        metricsService.incrementTraineeRegistrations();
        metricsService.incrementTraineeRegistrations();

        Counter counter = registry.find("gym.trainee.registrations").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    void incrementTrainerRegistrationsIncreasesCounter() {
        metricsService.incrementTrainerRegistrations();

        Counter counter = registry.find("gym.trainer.registrations").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void incrementTrainingSessionsIncreasesCounter() {
        metricsService.incrementTrainingSessions();
        metricsService.incrementTrainingSessions();
        metricsService.incrementTrainingSessions();

        Counter counter = registry.find("gym.training.sessions.created").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    void incrementLoginSuccessIncreasesSuccessCounter() {
        metricsService.incrementLoginSuccess();

        Counter counter = registry.find("gym.auth.login.attempts").tag("result", "success").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void incrementLoginFailureIncreasesFailureCounter() {
        metricsService.incrementLoginFailure();
        metricsService.incrementLoginFailure();

        Counter counter = registry.find("gym.auth.login.attempts").tag("result", "failure").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }
}
