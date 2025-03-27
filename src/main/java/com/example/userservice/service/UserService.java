package com.example.userservice.service;

import com.example.userservice.messaging.MessagePublisher;
import com.example.userservice.model.EmailNotification;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final MessagePublisher messagePublisher;

    public UserService(UserRepository userRepository, MessagePublisher messagePublisher) {
        this.userRepository = userRepository;
        this.messagePublisher = messagePublisher;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + user.getEmail());
        }

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getId());

        try {
            // Create email notification
            EmailNotification notification = EmailNotification.forNewUserRegistration(savedUser);

            // Send notification to RabbitMQ (o simplemente registra si RabbitMQ no está disponible)
            messagePublisher.publishEmailNotification(notification);
        } catch (Exception e) {
            // Log the error but don't fail the user creation
            logger.error("Failed to process email notification: {}", e.getMessage());
        }

        return savedUser;
    }

    @Transactional
    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(userDetails.getName());
                    existingUser.setPhone(userDetails.getPhone());

                    // Email cannot be changed in this example
                    // If changed, we would need to validate it's not already in use

                    return userRepository.save(existingUser);
                });
    }

    @Transactional
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    logger.info("User deleted: {}", id);
                    return true;
                })
                .orElse(false);
    }
}