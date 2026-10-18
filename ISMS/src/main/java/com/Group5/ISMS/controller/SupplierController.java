package com.Group5.ISMS.controller;

import com.Group5.ISMS.dto.ApiResponse;
import com.Group5.ISMS.dto.ProductDto;
import com.Group5.ISMS.dto.SupplierDto;
import com.Group5.ISMS.dto.UserDto;
import com.Group5.ISMS.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
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
    public ResponseEntity<ApiResponse<List<SupplierDto>>> getAllSuppliers(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        List<SupplierDto> suppliers = supplierService.getAllSuppliers(search);
        return ResponseEntity.ok(ApiResponse.ok("Suppliers retrieved successfully", suppliers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> getSupplierById(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        return supplierService.getSupplierById(id)
                .map(s -> ResponseEntity.ok(ApiResponse.ok("Supplier found", s)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Supplier not found with id: " + id)));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsBySupplier(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        List<ProductDto> products = supplierService.getProductsBySupplierId(id);
        return ResponseEntity.ok(ApiResponse.ok("Products for supplier retrieved", products));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierDto>> createSupplier(
            @RequestBody SupplierDto dto,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            SupplierDto created = supplierService.createSupplier(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Supplier created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating supplier: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierDto dto,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            SupplierDto updated = supplierService.updateSupplier(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Supplier updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating supplier: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(
            @PathVariable Long id,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(ApiResponse.ok("Supplier deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting supplier: " + e.getMessage()));
        }
    }
}
