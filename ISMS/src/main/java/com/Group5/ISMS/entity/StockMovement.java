package com.Group5.ISMS.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("stockMovements")
    private Product product;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    public StockMovement() {
    }

    public StockMovement(Product product, Integer quantityChange, LocalDateTime movementDate) {
        this.product = product;
        this.quantityChange = quantityChange;
        this.movementDate = movementDate != null ? movementDate : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.movementDate == null) {
            this.movementDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
