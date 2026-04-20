package com.epam.trainerworkload.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.dto.WorkloadActionType;
import com.epam.trainerworkload.model.TrainerWorkloadSummary;

class TrainerWorkloadServiceTest {

    private TrainerWorkloadService trainerWorkloadService;

    @BeforeEach
    void setUp() {
        trainerWorkloadService = new TrainerWorkloadService();
    }

    @Test
    void applyWorkloadChange_shouldCreateAndAccumulateMonthlyWorkload() {
        TrainerWorkloadRequest request = createRequest(
                "trainer.jane",
                "Jane",
                "Smith",
                true,
                LocalDate.of(2026, 4, 14),
                45,
                WorkloadActionType.ADD);

        trainerWorkloadService.applyWorkloadChange(request);

        TrainerWorkloadSummary summary = trainerWorkloadService.getByTrainerUsername("trainer.jane");

        assertNotNull(summary);
        assertEquals("trainer.jane", summary.getTrainerUsername());
        assertEquals("Jane", summary.getTrainerFirstName());
        assertEquals("Smith", summary.getTrainerLastName());
        assertEquals(true, summary.isTrainerActive());
        assertEquals(45, summary.getYears().get(2026).get(4));
    }

    @Test
    void applyWorkloadChange_shouldSubtractWithoutDroppingBelowZero() {
        trainerWorkloadService.applyWorkloadChange(createRequest(
                "trainer.jane",
                "Jane",
                "Smith",
                true,
                LocalDate.of(2026, 4, 14),
                30,
                WorkloadActionType.ADD));

        trainerWorkloadService.applyWorkloadChange(createRequest(
                "trainer.jane",
                "Janet",
                "Doe",
                false,
                LocalDate.of(2026, 4, 20),
                50,
                WorkloadActionType.DELETE));

        TrainerWorkloadSummary summary = trainerWorkloadService.getByTrainerUsername("trainer.jane");

        assertNotNull(summary);
        assertEquals("Janet", summary.getTrainerFirstName());
        assertEquals("Doe", summary.getTrainerLastName());
        assertEquals(false, summary.isTrainerActive());
        assertEquals(0, summary.getYears().get(2026).get(4));
    }

    @Test
    void getByTrainerUsername_shouldReturnNullForUnknownTrainer() {
        assertNull(trainerWorkloadService.getByTrainerUsername("missing.trainer"));
    }

    private static TrainerWorkloadRequest createRequest(
            String username,
            String firstName,
            String lastName,
            boolean active,
            LocalDate date,
            int duration,
            WorkloadActionType actionType) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName(firstName);
        request.setTrainerLastName(lastName);
        request.setTrainerActive(active);
        request.setTrainingDate(date);
        request.setTrainingDuration(duration);
        request.setActionType(actionType);
        return request;
    }
}