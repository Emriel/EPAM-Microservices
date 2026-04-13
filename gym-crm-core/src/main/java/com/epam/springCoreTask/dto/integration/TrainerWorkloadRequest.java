package com.epam.springCoreTask.dto.integration;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerWorkloadRequest {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean trainerActive;
    private LocalDate trainingDate;
    private Integer trainingDuration;
    private WorkloadActionType actionType;
}
