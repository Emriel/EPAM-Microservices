package com.epam.springCoreTask.facade;

import java.time.LocalDate;
import java.util.List;

import com.epam.springCoreTask.dto.request.TraineeRegistrationRequest;
import com.epam.springCoreTask.dto.request.TraineeUpdateRequest;
import com.epam.springCoreTask.dto.request.TrainerRegistrationRequest;
import com.epam.springCoreTask.dto.request.TrainerUpdateRequest;
import com.epam.springCoreTask.dto.request.TrainingRequest;
import com.epam.springCoreTask.dto.response.RegistrationResponse;
import com.epam.springCoreTask.dto.response.TraineeProfileResponse;
import com.epam.springCoreTask.dto.response.TrainerProfileResponse;
import com.epam.springCoreTask.dto.response.TrainerSummary;
import com.epam.springCoreTask.dto.response.TrainingResponse;
import com.epam.springCoreTask.model.Trainee;
import com.epam.springCoreTask.model.Trainer;
import com.epam.springCoreTask.model.User;

public interface GymFacade {
    RegistrationResponse createTraineeProfile(TraineeRegistrationRequest request);

    RegistrationResponse createTrainerProfile(TrainerRegistrationRequest request);

    void createTrainingSession(TrainingRequest request);

    TraineeProfileResponse updateTraineeProfile(String username, TraineeUpdateRequest request);

    TrainerProfileResponse updateTrainerProfile(String username, TrainerUpdateRequest request);

    TraineeProfileResponse getTraineeByUsername(String username);

    TrainerProfileResponse getTrainerByUsername(String username);

    void changeTraineeStatus(String username, boolean isActive);

    void changeTrainerStatus(String username, boolean isActive);

    void deleteTraineeByUsername(String username);

    List<TrainingResponse> getTraineeTrainingsWithCriteria(String traineeUsername, LocalDate fromDate,
            LocalDate toDate, String trainerName, String trainingTypeName);

    List<TrainingResponse> getTrainerTrainingsWithCriteria(String trainerUsername, LocalDate fromDate,
            LocalDate toDate, String traineeName);

    List<TrainerSummary> getTrainersNotAssignedToTrainee(String traineeUsername);

    List<TrainerSummary> updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames);

    User authenticateUser(String username, String password);

    void changeUserPassword(String username, String oldPassword, String newPassword);

    // Internal methods that still return entities (for service layer usage)
    /**
     * @deprecated Internal use only - controllers should use DTO methods
     */
    @Deprecated
    Trainee getTraineeEntityByUsername(String username);

    /**
     * @deprecated Internal use only - controllers should use DTO methods
     */
    @Deprecated
    Trainer getTrainerEntityByUsername(String username);
}
