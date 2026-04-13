package com.epam.trainerworkload.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerWorkloadSummary {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean trainerActive;
    @Builder.Default
    private Map<Integer, Map<Integer, Integer>> years = new ConcurrentHashMap<>();
}
