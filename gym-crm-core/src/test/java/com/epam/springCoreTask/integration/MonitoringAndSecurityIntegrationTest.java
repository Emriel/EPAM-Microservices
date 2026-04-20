package com.epam.springCoreTask.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
@AutoConfigureMockMvc
class MonitoringAndSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealthEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void actuatorHealthEndpointShowsDatabaseComponent() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.database").exists())
                .andExpect(jsonPath("$.components.database.status").value("UP"));
    }

    @Test
    void actuatorHealthEndpointShowsStorageFileComponent() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.storageFile").exists());
    }

    @Test
    void actuatorPrometheusEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    void actuatorPrometheusEndpointContainsGymMetrics() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("gym_trainee_registrations")));
    }

    @Test
    void protectedApiEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/trainees/username/someuser"))
                .andExpect(status().isUnauthorized());
    }
}
