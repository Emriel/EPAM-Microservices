package com.epam.springCoreTask.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Activation/Deactivation request")
public class ActivationRequest {

    @NotNull(message = "Active status is required")
    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
}
