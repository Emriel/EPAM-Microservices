package com.epam.trainerworkload.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TrainerWorkloadRequest {

    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean trainerActive;

    @NotNull
    private LocalDate trainingDate;

    @NotNull
    @Positive
    private Integer trainingDuration;

    @NotNull
    private WorkloadActionType actionType;
}
