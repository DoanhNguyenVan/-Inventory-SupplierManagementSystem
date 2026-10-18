package com.Group5.ISMS.service;

import com.Group5.ISMS.dto.LoginRequest;
import com.Group5.ISMS.dto.RegisterRequest;
import com.Group5.ISMS.dto.UserDto;
import com.Group5.ISMS.entity.User;
import com.Group5.ISMS.repository.UserRepository;
import com.Group5.ISMS.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters");
        }
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("Username can only contain alphanumeric characters, dots, underscores, and hyphens");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        String fullName = request.getFullName() != null ? request.getFullName().trim() : null;
        if (fullName != null && fullName.length() > 100) {
            throw new IllegalArgumentException("Full name cannot exceed 100 characters");
        }

        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email != null && !email.isEmpty()) {
            if (email.length() > 100) {
                throw new IllegalArgumentException("Email cannot exceed 100 characters");
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email '" + email + "' is already registered");
            }
        }

        String hashedPassword = PasswordUtil.hashPassword(request.getPassword());
        String role = "USER";

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

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    public List<UserDto> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        String clean = keyword.trim();
        return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(clean, clean, clean)
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUserRole(Long targetUserId, String newRole, Long currentAdminId) {
        if (newRole == null || newRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
        String normalizedRole = newRole.trim().toUpperCase();
        if (!normalizedRole.equals("ADMIN") && !normalizedRole.equals("STAFF") && !normalizedRole.equals("USER")) {
            throw new IllegalArgumentException("Invalid role. Permitted roles: ADMIN, STAFF, USER");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + targetUserId));

        if ("ADMIN".equals(targetUser.getRole()) && !"ADMIN".equals(normalizedRole)) {
            if (userRepository.countByRole("ADMIN") <= 1) {
                throw new IllegalArgumentException("Cannot demote the last remaining Administrator in the system");
            }
        }

        targetUser.setRole(normalizedRole);
        User updated = userRepository.save(targetUser);
        return new UserDto(updated);
    }

    @Transactional
    public void deleteUser(Long targetUserId, Long currentAdminId) {
        if (targetUserId.equals(currentAdminId)) {
            throw new IllegalArgumentException("You cannot delete your own logged-in account");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + targetUserId));

        if ("ADMIN".equals(targetUser.getRole()) && userRepository.countByRole("ADMIN") <= 1) {
            throw new IllegalArgumentException("Cannot delete the last remaining Administrator");
        }

        userRepository.delete(targetUser);
    }
}
