package com.typinggame.service;

import com.typinggame.domain.User;
import com.typinggame.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication service for user registration and login.
 * Simple implementation without password hashing or JWT tokens.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user.
     * 
     * @return User object if successful, null if username/email already exists
     */
    public User register(String username, String email, String password) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create and save new user
        User user = new User(username, email, password);
        return userRepository.save(user);
    }

    /**
     * Login user with username and password.
     * 
     * @return User object if credentials are valid, null otherwise
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Simple plain text password comparison
            if (user.getPassword().equals(password)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Get user profile by username.
     */
    public User getUserProfile(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Get user by ID.
     */
    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Update user statistics.
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
