package com.epam.springCoreTask.service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.epam.springCoreTask.model.User;

public interface UserService {

        <T> T authenticate(String username, String password,
                                Function<String, Optional<T>> entityFinder,
                                Function<T, User> userGetter,
                        String entityType);

        <T> void changePassword(String username, String oldPassword, String newPassword,
                                Function<String, Optional<T>> entityFinder,
                        Function<T, User> userGetter,
                        Consumer<T> saver,
                        String entityType);

        <T> void activateEntity(String username,
                        Function<String, T> entityGetter,
                        Function<T, User> userGetter,
                        Consumer<T> saver,
                        String entityType);

        <T> void deactivateEntity(String username,
                        Function<String, T> entityGetter,
                        Function<T, User> userGetter,
                        Consumer<T> saver,
                        String entityType);

        // Unified methods for direct User authentication and password management
        User authenticateUser(String username, String password);

        void changeUserPassword(String username, String oldPassword, String newPassword);

        void setActiveStatus(String username, boolean isActive);

                User getUserByUsername(String username);
}
