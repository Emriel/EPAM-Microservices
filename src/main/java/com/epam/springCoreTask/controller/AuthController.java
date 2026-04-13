package com.epam.springCoreTask.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.epam.springCoreTask.dto.request.ChangePasswordRequest;
import com.epam.springCoreTask.dto.request.LoginRequest;
import com.epam.springCoreTask.dto.response.LoginResponse;
import com.epam.springCoreTask.facade.GymFacade;
import com.epam.springCoreTask.model.User;
import com.epam.springCoreTask.security.jwt.JwtService;
import com.epam.springCoreTask.security.jwt.JwtTokenBlacklistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthController {

    private final GymFacade gymFacade;
    private final JwtService jwtService;
    private final JwtTokenBlacklistService tokenBlacklistService;

    @Operation(summary = "User login", description = "Authenticate user with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", 
                     content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        User user = gymFacade.authenticateUser(request.getUsername(), request.getPassword());
        String accessToken = jwtService.generateToken(user.getUsername());

        log.info("User login successful: {}", request.getUsername());
        return ResponseEntity.ok(new LoginResponse(user.getUsername(), accessToken));
    }

    @Operation(summary = "Change password", description = "Change user password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", 
                     content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "404", description = "User not found", 
                     content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", request.getUsername());
        
        gymFacade.changeUserPassword(request.getUsername(), 
                                     request.getOldPassword(), 
                                     request.getNewPassword());
        log.info("User password changed successfully: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "User logout", description = "Invalidate the current JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, Authentication authentication) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
        }

        log.info("User logout successful: {}", authentication != null ? authentication.getName() : "unknown");
        return ResponseEntity.ok().build();
    }
}
