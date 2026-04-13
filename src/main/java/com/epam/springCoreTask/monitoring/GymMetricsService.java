package com.epam.springCoreTask.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GymMetricsService {

    private final MeterRegistry meterRegistry;

    private Counter traineeRegistrationsCounter;
    private Counter trainerRegistrationsCounter;
    private Counter trainingSessionsCounter;
    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;

    @PostConstruct
    public void initMetrics() {
        traineeRegistrationsCounter = Counter.builder("gym.trainee.registrations")
                .description("Total number of trainee registrations")
                .register(meterRegistry);

        trainerRegistrationsCounter = Counter.builder("gym.trainer.registrations")
                .description("Total number of trainer registrations")
                .register(meterRegistry);

        trainingSessionsCounter = Counter.builder("gym.training.sessions.created")
                .description("Total number of training sessions created")
                .register(meterRegistry);

        loginSuccessCounter = Counter.builder("gym.auth.login.attempts")
                .description("Number of successful login attempts")
                .tag("result", "success")
                .register(meterRegistry);

        loginFailureCounter = Counter.builder("gym.auth.login.attempts")
                .description("Number of failed login attempts")
                .tag("result", "failure")
                .register(meterRegistry);

        log.info("Gym custom metrics registered");
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrationsCounter.increment();
        log.debug("Incremented gym.trainee.registrations counter");
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrationsCounter.increment();
        log.debug("Incremented gym.trainer.registrations counter");
    }

    public void incrementTrainingSessions() {
        trainingSessionsCounter.increment();
        log.debug("Incremented gym.training.sessions.created counter");
    }

    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
        log.debug("Incremented gym.auth.login.attempts[result=success] counter");
    }

    public void incrementLoginFailure() {
        loginFailureCounter.increment();
        log.debug("Incremented gym.auth.login.attempts[result=failure] counter");
    }
}
