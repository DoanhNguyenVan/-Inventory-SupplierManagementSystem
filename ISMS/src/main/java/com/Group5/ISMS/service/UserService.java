package com.Group5.ISMS.service;

import com.Group5.ISMS.dto.LoginRequest;
import com.Group5.ISMS.dto.RegisterRequest;
import com.Group5.ISMS.dto.UserDto;
import com.Group5.ISMS.entity.User;
import com.Group5.ISMS.repository.UserRepository;
import com.Group5.ISMS.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered");
        }

        String hashedPassword = PasswordUtil.hashPassword(request.getPassword());
        String role = (request.getRole() != null && !request.getRole().trim().isEmpty())
                ? request.getRole().trim().toUpperCase()
                : "USER";

        User user = new User(
                username,
                hashedPassword,
                request.getFullName(),
                email,
                role
        );

        User saved = userRepository.save(user);
        return new UserDto(saved);
    }

    public UserDto login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String identifier = request.getUsername().trim();

        // Allow login by username OR email
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(identifier);
        }

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();
        boolean matches = PasswordUtil.verifyPassword(request.getPassword(), user.getPassword());
        if (!matches) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return new UserDto(user);
    }

    public Optional<UserDto> findById(Long userId) {
        return userRepository.findById(userId).map(UserDto::new);
    }

    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(UserDto::new);
    }
}
