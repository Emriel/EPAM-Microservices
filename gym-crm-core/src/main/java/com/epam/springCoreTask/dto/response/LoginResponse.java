package com.epam.springCoreTask.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Authentication response")
public class LoginResponse {

    @Schema(description = "Authenticated username", example = "john.doe")
    private String username;

    @Schema(description = "JWT bearer token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    public LoginResponse(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }
}