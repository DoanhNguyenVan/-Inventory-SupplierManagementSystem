package com.Group5.ISMS.controller;

import com.Group5.ISMS.dto.ApiResponse;
import com.Group5.ISMS.dto.StockMovementDto;
import com.Group5.ISMS.dto.UserDto;
import com.Group5.ISMS.service.StockMovementService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute(AuthController.SESSION_USER_KEY) != null;
    }

    private boolean canModify(HttpSession session) {
        if (session == null) return false;
        UserDto user = (UserDto) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) return false;
        String role = user.getRole();
        return "ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> getAllStockMovements(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        List<StockMovementDto> list = stockMovementService.getAllStockMovements();
        return ResponseEntity.ok(ApiResponse.ok("Stock movements retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockMovementDto>> getStockMovementById(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        return stockMovementService.getStockMovementById(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok("Stock movement found", m)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Stock movement not found with id: " + id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockMovementDto>> recordStockMovement(
            @RequestBody StockMovementDto dto,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        UserDto currentUser = (UserDto) session.getAttribute(AuthController.SESSION_USER_KEY);
        Long userId = currentUser != null ? currentUser.getUserId() : null;

        try {
            StockMovementDto created = stockMovementService.recordStockMovement(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Stock movement recorded successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error recording stock movement: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStockMovement(
            @PathVariable Long id,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            stockMovementService.deleteStockMovement(id);
            return ResponseEntity.ok(ApiResponse.ok("Stock movement deleted and inventory reverted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting stock movement: " + e.getMessage()));
        }
    }
}
