package com.Group5.ISMS.controller;

import com.Group5.ISMS.dto.ApiResponse;
import com.Group5.ISMS.dto.ProductDto;
import com.Group5.ISMS.dto.UserDto;
import com.Group5.ISMS.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
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
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        List<ProductDto> products = productService.getAllProducts(search);
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved successfully", products));
    }

    @GetMapping("/trash")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getTrashProducts(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }
        List<ProductDto> trash = productService.getDeletedProducts(search);
        return ResponseEntity.ok(ApiResponse.ok("Recycle bin retrieved successfully", trash));
    }

    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getProductCounts(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        counts.put("active", productService.countProducts());
        counts.put("trash", productService.countDeletedProducts());
        return ResponseEntity.ok(ApiResponse.ok("Product counts", counts));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        return productService.getProductById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Product found", p)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Product not found with id: " + id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@RequestBody ProductDto dto, HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            ProductDto created = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Product created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating product: " + e.getMessage()));
        }
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto dto,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            ProductDto updated = productService.updateProduct(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating product: " + e.getMessage()));
        }
    }

    @PutMapping("/{id:\\d+}/restore")
    public ResponseEntity<ApiResponse<ProductDto>> restoreProduct(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> body,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        Integer newQuantity = null;
        if (body != null && body.containsKey("quantityInStock") && body.get("quantityInStock") != null) {
            try {
                newQuantity = Integer.parseInt(body.get("quantityInStock").toString().trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid quantity format!"));
            }
        }

        try {
            ProductDto restored = productService.restoreProduct(id, newQuantity);
            return ResponseEntity.ok(ApiResponse.ok("Product restored successfully from recycle bin", restored));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error restoring product: " + e.getMessage()));
        }
    }

    @PutMapping("/{id:\\d+}/status")
    public ResponseEntity<ApiResponse<ProductDto>> updateProductStatus(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body,
            HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        String targetStatus = (body != null) ? body.get("status") : null;
        try {
            ProductDto updated = productService.toggleProductStatus(id, targetStatus);
            return ResponseEntity.ok(ApiResponse.ok("Product status updated to " + updated.getStatus(), updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating status: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.ok("Product moved to recycle bin successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id:\\d+}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDeleteProduct(@PathVariable Long id, HttpSession session) {
        if (!canModify(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Role 'USER' is restricted to read-only access."));
        }

        try {
            productService.permanentDeleteProduct(id);
            return ResponseEntity.ok(ApiResponse.ok("Product permanently deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting product permanently: " + e.getMessage()));
        }
    }
}
