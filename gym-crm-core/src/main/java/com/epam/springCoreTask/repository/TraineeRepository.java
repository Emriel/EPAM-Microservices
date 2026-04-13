package com.epam.springCoreTask.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.epam.springCoreTask.model.Trainee;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUser_Username(String username);

    List<Trainee> findByUser_UsernameIn(List<String> usernames);

    boolean existsByUser_Username(String username);

    @Query("SELECT t.user.username FROM Trainee t")
    List<String> findAllUsernames();
}
