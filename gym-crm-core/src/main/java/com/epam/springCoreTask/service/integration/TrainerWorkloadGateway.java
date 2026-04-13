package com.epam.springCoreTask.service.integration;

import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.epam.springCoreTask.client.TrainerWorkloadClient;
import com.epam.springCoreTask.dto.integration.TrainerWorkloadRequest;
import com.epam.springCoreTask.dto.integration.WorkloadActionType;
import com.epam.springCoreTask.model.Trainer;
import com.epam.springCoreTask.security.jwt.JwtService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadGateway {

    private static final String SERVICE_SUBJECT = "gym-crm-service";
    private static final String TRANSACTION_ID = "transactionId";

    private final TrainerWorkloadClient trainerWorkloadClient;
    private final JwtService jwtService;

    @CircuitBreaker(name = "trainerWorkloadService", fallbackMethod = "fallbackReportTrainingAdded")
    public void reportTrainingAdded(Trainer trainer, LocalDate trainingDate, int trainingDuration) {
        String token = jwtService.generateToken(SERVICE_SUBJECT);
        String transactionId = resolveTransactionId();

        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(trainer.getUser().getUsername())
                .trainerFirstName(trainer.getUser().getFirstName())
                .trainerLastName(trainer.getUser().getLastName())
                .trainerActive(trainer.getUser().isActive())
                .trainingDate(trainingDate)
                .trainingDuration(trainingDuration)
                .actionType(WorkloadActionType.ADD)
                .build();

        trainerWorkloadClient.reportWorkload("Bearer " + token, transactionId, request);
        log.info("Trainer workload reported for trainer={} transactionId={}",
                trainer.getUser().getUsername(), transactionId);
    }

    @CircuitBreaker(name = "trainerWorkloadService", fallbackMethod = "fallbackReportTrainingDeleted")
    public void reportTrainingDeleted(Trainer trainer, LocalDate trainingDate, int trainingDuration) {
        String token = jwtService.generateToken(SERVICE_SUBJECT);
        String transactionId = resolveTransactionId();

        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(trainer.getUser().getUsername())
                .trainerFirstName(trainer.getUser().getFirstName())
                .trainerLastName(trainer.getUser().getLastName())
                .trainerActive(trainer.getUser().isActive())
                .trainingDate(trainingDate)
                .trainingDuration(trainingDuration)
                .actionType(WorkloadActionType.DELETE)
                .build();

        trainerWorkloadClient.reportWorkload("Bearer " + token, transactionId, request);
        log.info("Trainer workload delete reported for trainer={} transactionId={}",
                trainer.getUser().getUsername(), transactionId);
    }

    @SuppressWarnings("unused")
    private void fallbackReportTrainingAdded(Trainer trainer, LocalDate trainingDate, int trainingDuration,
            Throwable ex) {
        log.warn("Trainer workload reporting failed for trainer={} due to: {}",
                trainer.getUser().getUsername(), ex.getMessage());
    }

    @SuppressWarnings("unused")
    private void fallbackReportTrainingDeleted(Trainer trainer, LocalDate trainingDate, int trainingDuration,
            Throwable ex) {
        log.warn("Trainer workload delete reporting failed for trainer={} due to: {}",
                trainer.getUser().getUsername(), ex.getMessage());
    }

    private String resolveTransactionId() {
        String transactionId = MDC.get(TRANSACTION_ID);
        if (transactionId == null || transactionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return transactionId;
    }
}
