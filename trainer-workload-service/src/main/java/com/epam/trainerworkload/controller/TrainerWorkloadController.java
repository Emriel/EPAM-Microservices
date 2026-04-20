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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/workloads")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Trainer Workload", description = "Trainer workload reporting endpoints")
public class TrainerWorkloadController {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final TrainerWorkloadService trainerWorkloadService;

    @Operation(summary = "Report workload change", description = "Adds or removes workload minutes for a trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workload update accepted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = String.class)))
    })
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

    @Operation(summary = "Get trainer workload", description = "Fetches the current workload summary for a trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workload summary found"),
            @ApiResponse(responseCode = "404", description = "Trainer workload not found", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{trainerUsername}")
    public ResponseEntity<TrainerWorkloadSummary> getTrainerWorkload(@PathVariable String trainerUsername) {
        TrainerWorkloadSummary summary = trainerWorkloadService.getByTrainerUsername(trainerUsername);
        return summary == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(summary);
    }
}
