package com.epam.springCoreTask.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.epam.springCoreTask.exception.AuthenticationException;
import com.epam.springCoreTask.exception.EntityNotFoundException;
import com.epam.springCoreTask.exception.ValidationException;
import com.epam.springCoreTask.model.User;
import com.epam.springCoreTask.repository.UserRepository;
import com.epam.springCoreTask.service.UserService;
import com.epam.springCoreTask.util.ValidationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.login.max-attempts:3}")
    private int maxLoginAttempts;

    @Value("${app.security.login.lock-minutes:5}")
    private int lockMinutes;

    @Override
    public <T> T authenticate(String username, String password,
            Function<String, Optional<T>> entityFinder,
            Function<T, User> userGetter, String entityType) {
        log.debug("Authenticating {}: username={}", entityType, username);

        validationUtil.validateNotBlank(username, "Username");
        validationUtil.validateNotBlank(password, "Password");

        T entity = entityFinder.apply(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed for {}: username={}", entityType, username);
                    return new AuthenticationException("Invalid username or password");
                });

        User user = userGetter.apply(entity);
        validateAccountEligibility(user);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            registerFailedAttempt(user);
            log.warn("Authentication failed for {}: username={}", entityType, username);
            throw new AuthenticationException("Invalid username or password");
        }

        resetFailedAttempts(user);

        log.info("{} authenticated successfully: username={}",
                entityType.substring(0, 1).toUpperCase() + entityType.substring(1), username);
        return entity;
    }

    @Override
    public <T> void changePassword(String username, String oldPassword, String newPassword,
            Function<String, Optional<T>> entityFinder,
            Function<T, User> userGetter,
            Consumer<T> saver,
            String entityType) {
        log.debug("Changing password for {}: username={}", entityType, username);

        validationUtil.validateNotBlank(username, "Username");
        validationUtil.validateNotBlank(oldPassword, "Old password");
        validationUtil.validateNotBlank(newPassword, "New password");

        T entity = entityFinder.apply(username)
                .orElseThrow(() -> {
                    log.warn("Password change failed - invalid credentials for {}: username={}",
                            entityType, username);
                    return new AuthenticationException("Invalid username or old password does not match");
                });

        User user = userGetter.apply(entity);
        validateAccountEligibility(user);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid username or old password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        saver.accept(entity);

        log.info("Password changed successfully for {}: username={}", entityType, username);
    }

    @Override
    public <T> void activateEntity(String username,
            Function<String, T> entityGetter,
            Function<T, User> userGetter,
            Consumer<T> saver,
            String entityType) {
        log.debug("Activating {}: username={}", entityType, username);

        validationUtil.validateNotBlank(username, "Username");

        T entity = entityGetter.apply(username);
        User user = userGetter.apply(entity);

        if (user.isActive()) {
            log.warn("{} is already active: username={}", entityType, username);
            throw new ValidationException(
                    entityType.substring(0, 1).toUpperCase() + entityType.substring(1) + " is already active");
        }

        user.setActive(true);
        saver.accept(entity);

        log.info("{} activated successfully: username={}",
                entityType.substring(0, 1).toUpperCase() + entityType.substring(1), username);
    }

    @Override
    public <T> void deactivateEntity(String username,
            Function<String, T> entityGetter,
            Function<T, User> userGetter,
            Consumer<T> saver,
            String entityType) {
        log.debug("Deactivating {}: username={}", entityType, username);

        validationUtil.validateNotBlank(username, "Username");

        T entity = entityGetter.apply(username);
        User user = userGetter.apply(entity);

        if (!user.isActive()) {
            log.warn("{} is already inactive: username={}", entityType, username);
            throw new ValidationException(
                    entityType.substring(0, 1).toUpperCase() + entityType.substring(1) + " is already inactive");
        }

        user.setActive(false);
        saver.accept(entity);

        log.info("{} deactivated successfully: username={}",
                entityType.substring(0, 1).toUpperCase() + entityType.substring(1), username);
    }

    @Override
    public User authenticateUser(String username, String password) {
        log.debug("Authenticating user: username={}", username);

        validationUtil.validateNotBlank(username, "Username");
        validationUtil.validateNotBlank(password, "Password");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed for user: username={}", username);
                    return new AuthenticationException("Invalid username or password");
                });

        validateAccountEligibility(user);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            registerFailedAttempt(user);
            log.warn("Authentication failed for user: username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        resetFailedAttempts(user);

        log.info("User authenticated successfully: username={}", username);
        return user;
    }

    @Override
    public void changeUserPassword(String username, String oldPassword, String newPassword) {
        log.debug("Changing password for user: username={}", username);

        validationUtil.validateNotBlank(username, "Username");
        validationUtil.validateNotBlank(oldPassword, "Old password");
        validationUtil.validateNotBlank(newPassword, "New password");

        User user = getUserByUsername(username);
        validateAccountEligibility(user);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed - invalid credentials for user: username={}", username);
            throw new AuthenticationException("Invalid username or old password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        resetFailedAttempts(user);
        userRepository.save(user);

        log.info("Password changed successfully for user: username={}", username);
    }

    @Override
    public void setActiveStatus(String username, boolean isActive) {
        log.debug("Setting active status for user: username={}, isActive={}", username, isActive);

        validationUtil.validateNotBlank(username, "Username");

        User user = getUserByUsername(username);
        user.setActive(isActive);
        userRepository.save(user);

        log.info("Active status set to {} for user: username={}", isActive, username);
    }

    @Override
    public User getUserByUsername(String username) {
        validationUtil.validateNotBlank(username, "Username");
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private void validateAccountEligibility(User user) {
        if (!user.isActive()) {
            throw new AuthenticationException("User account is inactive");
        }

        if (isAccountLocked(user)) {
            throw new AuthenticationException("User is blocked until " + user.getAccountLockedUntil());
        }
    }

    private boolean isAccountLocked(User user) {
        LocalDateTime accountLockedUntil = user.getAccountLockedUntil();
        if (accountLockedUntil == null) {
            return false;
        }

        if (accountLockedUntil.isAfter(LocalDateTime.now())) {
            return true;
        }

        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        return false;
    }

    private void registerFailedAttempt(User user) {
        int nextAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(nextAttempts);

        if (nextAttempts >= maxLoginAttempts) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() == 0 && user.getAccountLockedUntil() == null) {
            return;
        }

        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);
    }
}
