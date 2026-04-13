package com.epam.springCoreTask.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration response with credentials")
public class RegistrationResponse {

    @Schema(description = "Generated username", example = "john.doe")
    private String username;

    @Schema(description = "Generated password", example = "aBc123XyZ")
    private String password;

    @Schema(description = "JWT bearer token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    public RegistrationResponse(String username, String password) {
        this.username = username;
        this.password = password;
        this.tokenType = "Bearer";
    }

    public RegistrationResponse(String username, String password, String accessToken) {
        this.username = username;
        this.password = password;
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }
}
