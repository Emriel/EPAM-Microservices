package com.epam.trainerworkload.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.epam.trainerworkload.model.TrainerWorkloadSummary;
import com.epam.trainerworkload.security.JwtValidationService;
import com.epam.trainerworkload.service.TrainerWorkloadService;

@WebMvcTest(TrainerWorkloadController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerWorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerWorkloadService trainerWorkloadService;

    @MockBean
    private JwtValidationService jwtValidationService;

    @Test
    void reportTrainingWorkload_shouldReturnOkAndDelegateToService() throws Exception {
        String requestBody = "{\n"
                + "  \"trainerUsername\": \"trainer.jane\",\n"
                + "  \"trainerFirstName\": \"Jane\",\n"
                + "  \"trainerLastName\": \"Smith\",\n"
                + "  \"trainerActive\": true,\n"
                + "  \"trainingDate\": \"2026-04-14\",\n"
                + "  \"trainingDuration\": 45,\n"
                + "  \"actionType\": \"ADD\"\n"
                + "}";

        mockMvc.perform(post("/api/workloads/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Transaction-Id", "tx-123"))
                .andExpect(status().isOk());

        verify(trainerWorkloadService).applyWorkloadChange(any());
    }

    @Test
    void reportTrainingWorkload_shouldRejectInvalidPayload() throws Exception {
        String requestBody = "{\n"
                + "  \"trainerUsername\": \"\",\n"
                + "  \"trainingDate\": \"2026-04-14\",\n"
                + "  \"trainingDuration\": 0,\n"
                + "  \"actionType\": \"ADD\"\n"
                + "}";

        mockMvc.perform(post("/api/workloads/report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainerWorkload_shouldReturnSummaryWhenPresent() throws Exception {
        TrainerWorkloadSummary summary = TrainerWorkloadSummary.builder()
                .trainerUsername("trainer.jane")
                .trainerFirstName("Jane")
                .trainerLastName("Smith")
                .trainerActive(true)
                .build();
        summary.getYears().put(2026, Map.of(4, 45));

        when(trainerWorkloadService.getByTrainerUsername(eq("trainer.jane"))).thenReturn(summary);

        mockMvc.perform(get("/api/workloads/{trainerUsername}", "trainer.jane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("trainer.jane"))
                .andExpect(jsonPath("$.trainerFirstName").value("Jane"))
                .andExpect(jsonPath("$.trainerLastName").value("Smith"))
                .andExpect(jsonPath("$.trainerActive").value(true))
                .andExpect(jsonPath("$.years['2026']['4']").value(45));
    }

    @Test
    void getTrainerWorkload_shouldReturnNotFoundWhenMissing() throws Exception {
        when(trainerWorkloadService.getByTrainerUsername(eq("missing"))).thenReturn(null);

        mockMvc.perform(get("/api/workloads/{trainerUsername}", "missing"))
                .andExpect(status().isNotFound());
    }
}