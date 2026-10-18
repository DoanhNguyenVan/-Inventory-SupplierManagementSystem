package com.Group5.ISMS.controller;

import com.Group5.ISMS.dto.ApiResponse;
import com.Group5.ISMS.dto.DashboardStatsDto;
import com.Group5.ISMS.service.ProductService;
import com.Group5.ISMS.service.StockMovementService;
import com.Group5.ISMS.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final StockMovementService stockMovementService;

    public DashboardController(ProductService productService,
                               SupplierService supplierService,
                               StockMovementService stockMovementService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.stockMovementService = stockMovementService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats(jakarta.servlet.http.HttpSession session) {
        if (session == null || session.getAttribute(AuthController.SESSION_USER_KEY) == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required. Please sign in."));
        }
        long totalProducts = productService.countProducts();
        long totalSuppliers = supplierService.countSuppliers();
        long lowStockCount = productService.countLowStock(10);
        long totalMovements = stockMovementService.countMovements();

        DashboardStatsDto dto = new DashboardStatsDto(totalProducts, totalSuppliers, lowStockCount, totalMovements);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard statistics retrieved", dto));
    }
}
