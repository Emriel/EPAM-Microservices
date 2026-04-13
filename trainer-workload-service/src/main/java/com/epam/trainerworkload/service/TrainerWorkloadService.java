package com.epam.trainerworkload.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.dto.WorkloadActionType;
import com.epam.trainerworkload.model.TrainerWorkloadSummary;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TrainerWorkloadService {

    private final Map<String, TrainerWorkloadSummary> storage = new ConcurrentHashMap<>();

    public void applyWorkloadChange(TrainerWorkloadRequest request) {
        TrainerWorkloadSummary summary = storage.computeIfAbsent(
                request.getTrainerUsername(),
                username -> TrainerWorkloadSummary.builder()
                        .trainerUsername(username)
                        .trainerFirstName(request.getTrainerFirstName())
                        .trainerLastName(request.getTrainerLastName())
                        .trainerActive(request.getTrainerActive())
                        .build());

        summary.setTrainerFirstName(request.getTrainerFirstName());
        summary.setTrainerLastName(request.getTrainerLastName());
        summary.setTrainerActive(request.getTrainerActive());

        LocalDate date = request.getTrainingDate();
        int year = date.getYear();
        int month = date.getMonthValue();
        int duration = request.getTrainingDuration();

        Map<Integer, Integer> monthlyByYear = summary.getYears().computeIfAbsent(year,
                ignored -> new ConcurrentHashMap<>());
        int delta = request.getActionType() == WorkloadActionType.ADD ? duration : -duration;
        int updatedMinutes = Math.max(0, monthlyByYear.getOrDefault(month, 0) + delta);

        monthlyByYear.put(month, updatedMinutes);
        log.info("Workload {} applied for trainer={}, year={}, month={}, totalMinutes={}",
                request.getActionType(), request.getTrainerUsername(), year, month, updatedMinutes);
    }

    public TrainerWorkloadSummary getByTrainerUsername(String trainerUsername) {
        return storage.get(trainerUsername);
    }
}
