package com.epam.trainerworkload.controller;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.model.TrainerWorkloadSummary;
import com.epam.trainerworkload.service.TrainerWorkloadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/workloads")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TrainerWorkloadController {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final TrainerWorkloadService trainerWorkloadService;

    @PostMapping("/report")
    public ResponseEntity<Void> reportTrainingWorkload(
            @Valid @RequestBody TrainerWorkloadRequest request,
            @RequestHeader(value = TRANSACTION_ID_HEADER, required = false) String transactionId) {
        String resolvedTransactionId = transactionId == null || transactionId.isBlank()
                ? UUID.randomUUID().toString()
                : transactionId;

        try {
            MDC.put("transactionId", resolvedTransactionId);
            log.info("Received workload update for trainer={} action={}",
                    request.getTrainerUsername(), request.getActionType());

            trainerWorkloadService.applyWorkloadChange(request);
            return ResponseEntity.ok().build();
        } finally {
            MDC.remove("transactionId");
        }
    }

    @GetMapping("/{trainerUsername}")
    public ResponseEntity<TrainerWorkloadSummary> getTrainerWorkload(@PathVariable String trainerUsername) {
        TrainerWorkloadSummary summary = trainerWorkloadService.getByTrainerUsername(trainerUsername);
        return summary == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(summary);
    }
}
