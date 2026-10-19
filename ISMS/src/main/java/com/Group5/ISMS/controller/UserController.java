package com.Group5.ISMS.controller;

import com.Group5.ISMS.dto.ApiResponse;
import com.Group5.ISMS.dto.UserDto;
import com.Group5.ISMS.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private UserDto checkAdmin(HttpSession session) {
        if (session == null) {
            return null;
        }
        UserDto currentUser = (UserDto) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return null;
        }
        return currentUser;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsers(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session) {
        UserDto admin = checkAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator privilege required"));
        }

        List<UserDto> users = userService.searchUsers(search);
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id, HttpSession session) {
        UserDto admin = checkAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator privilege required"));
        }

        return userService.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.ok("User found", user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found with id: " + id)));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        UserDto admin = checkAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator privilege required"));
        }

        String newRole = body.get("role");
        if (newRole == null || newRole.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Role cannot be empty"));
        }

        try {
            UserDto updated = userService.updateUserRole(id, newRole, admin.getUserId());
            return ResponseEntity.ok(ApiResponse.ok("User role updated to " + updated.getRole(), updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating user role: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id, HttpSession session) {
        UserDto admin = checkAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator privilege required"));
        }

        try {
            userService.deleteUser(id, admin.getUserId());
            return ResponseEntity.ok(ApiResponse.ok("User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting user: " + e.getMessage()));
        }
    }

    @GetMapping("/locked")
    public ResponseEntity<ApiResponse<List<UserDto>>> getLockedUsers(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session) {
        UserDto admin = checkAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator privilege required"));
        }

        List<UserDto> users = userService.searchLockedUsers(search);
        return ResponseEntity.ok(ApiResponse.ok("Locked users retrieved successfully", users));
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<UserDto>> restoreUser(@PathVariable Long id, HttpSession session) {
        UserDto admin = checkAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator privilege required"));
        }

        try {
            UserDto restored = userService.restoreUser(id);
            return ResponseEntity.ok(ApiResponse.ok("User \"" + restored.getUsername() + "\" has been unlocked successfully", restored));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error restoring user: " + e.getMessage()));
        }
    }

    @GetMapping("/locked/count")
    public ResponseEntity<ApiResponse<Long>> getLockedCount(HttpSession session) {
        UserDto admin = checkAdmin(session);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Administrator privilege required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Locked user count", userService.countLockedUsers()));
    }
}
