package com.Group5.ISMS.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@SQLDelete(sql = "UPDATE stock_movements SET is_deleted = 1 WHERE movement_id = ?")
@SQLRestriction("is_deleted = 0")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("stockMovements")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("password")
    private User user;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "bit default 0")
    private Boolean isDeleted = false;

    public StockMovement() {
    }

    public StockMovement(Product product, Integer quantityChange, LocalDateTime movementDate) {
        this(product, quantityChange, movementDate, null);
    }

    public StockMovement(Product product, Integer quantityChange, LocalDateTime movementDate, User user) {
        this.product = product;
        this.quantityChange = quantityChange;
        this.movementDate = movementDate != null ? movementDate : LocalDateTime.now();
        this.user = user;
        this.isDeleted = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.movementDate == null) {
            this.movementDate = LocalDateTime.now();
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
