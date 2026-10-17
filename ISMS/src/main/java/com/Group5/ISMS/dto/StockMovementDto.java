package com.Group5.ISMS.dto;

import com.Group5.ISMS.entity.StockMovement;
import java.time.LocalDateTime;

public class StockMovementDto {

    private Long movementId;
    private Long productId;
    private String productName;
    private Integer quantityChange;
    private LocalDateTime movementDate;

    public StockMovementDto() {
    }

    public StockMovementDto(StockMovement sm) {
        if (sm != null) {
            this.movementId = sm.getMovementId();
            if (sm.getProduct() != null) {
                this.productId = sm.getProduct().getProductId();
                this.productName = sm.getProduct().getProductName();
            }
            this.quantityChange = sm.getQuantityChange();
            this.movementDate = sm.getMovementDate();
        }
    }

    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }
}
