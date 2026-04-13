package com.epam.springCoreTask.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.epam.springCoreTask.config.LoggingInterceptor;
import com.epam.springCoreTask.exception.AuthenticationException;
import com.epam.springCoreTask.facade.GymFacade;
import com.epam.springCoreTask.security.GymUserDetailsService;
import com.epam.springCoreTask.security.jwt.JwtAuthenticationFilter;
import com.epam.springCoreTask.security.jwt.JwtService;
import com.epam.springCoreTask.security.jwt.JwtTokenBlacklistService;
import com.epam.springCoreTask.model.Trainee;
import com.epam.springCoreTask.model.Trainer;
import com.epam.springCoreTask.model.User;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GymFacade gymFacade;

    @MockBean
        private JwtService jwtService;

        @MockBean
        private JwtTokenBlacklistService tokenBlacklistService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private GymUserDetailsService gymUserDetailsService;

    @MockBean
    private LoggingInterceptor loggingInterceptor;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() throws Exception {
        when(loggingInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        
        User traineeUser = new User(1L, "John", "Doe", "john.doe", "password123", true);
        trainee = new Trainee();
        trainee.setUser(traineeUser);

        User trainerUser = new User(2L, "Jane", "Smith", "jane.smith", "password123", true);
        trainer = new Trainer();
        trainer.setUser(trainerUser);
    }

    @Test
    void testLogin_AsTrainee_Success() throws Exception {
        User user = new User(1L, "John", "Doe", "john.doe", "password123", true);
        when(gymFacade.authenticateUser("john.doe", "password123")).thenReturn(user);
        when(jwtService.generateToken("john.doe")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\":\"john.doe\"," +
                        "\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(gymFacade).authenticateUser("john.doe", "password123");
    }

    @Test
    void testLogin_AsTrainer_Success() throws Exception {
        User user = new User(2L, "Jane", "Smith", "jane.smith", "password123", true);
        when(gymFacade.authenticateUser("jane.smith", "password123")).thenReturn(user);
        when(jwtService.generateToken("jane.smith")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\":\"jane.smith\"," +
                        "\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane.smith"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));

        verify(gymFacade).authenticateUser("jane.smith", "password123");
    }

    @Test
    void testLogin_InvalidCredentials_Unauthorized() throws Exception {
        when(gymFacade.authenticateUser("invalid", "wrong"))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\":\"invalid\"," +
                        "\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_MissingUsername_BadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChangePassword_AsTrainee_Success() throws Exception {
        doNothing().when(gymFacade).changeUserPassword("john.doe", "oldPass", "newPass");

        String requestBody = "{\n" +
                "  \"username\": \"john.doe\",\n" +
                "  \"oldPassword\": \"oldPass\",\n" +
                "  \"newPassword\": \"newPass\"\n" +
                "}";

        mockMvc.perform(put("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        verify(gymFacade).changeUserPassword("john.doe", "oldPass", "newPass");
    }

    @Test
    void testChangePassword_AsTrainer_Success() throws Exception {
        doNothing().when(gymFacade).changeUserPassword("jane.smith", "oldPass", "newPass");

        String requestBody = "{\n" +
                "  \"username\": \"jane.smith\",\n" +
                "  \"oldPassword\": \"oldPass\",\n" +
                "  \"newPassword\": \"newPass\"\n" +
                "}";

        mockMvc.perform(put("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        verify(gymFacade).changeUserPassword("jane.smith", "oldPass", "newPass");
    }

    @Test
    void testChangePassword_InvalidOldPassword_Unauthorized() throws Exception {
        doThrow(new AuthenticationException("Invalid old password"))
                .when(gymFacade).changeUserPassword("john.doe", "wrongOld", "newPass");

        String requestBody = "{\n" +
                "  \"username\": \"john.doe\",\n" +
                "  \"oldPassword\": \"wrongOld\",\n" +
                "  \"newPassword\": \"newPass\"\n" +
                "}";

        mockMvc.perform(put("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}
