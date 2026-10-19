package com.Group5.ISMS.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    @JsonIgnoreProperties("products")
    private Supplier supplier;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "quantity_in_stock")
    private Integer quantityInStock = 0;

    @Column(name = "status", length = 30)
    private String status = "ACTIVE"; // "ACTIVE" (Đang kinh doanh), "SUSPENDED" (Tạm ngưng)

    @Column(name = "is_deleted", nullable = false, columnDefinition = "bit default 0")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "product")
    @JsonIgnoreProperties("product")
    private List<StockMovement> stockMovements = new ArrayList<>();

    public Product() {
    }

    public Product(Supplier supplier, String productName, Integer quantityInStock) {
        this.supplier = supplier;
        this.productName = productName;
        this.quantityInStock = quantityInStock != null ? quantityInStock : 0;
        this.status = "ACTIVE";
        this.isDeleted = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.quantityInStock == null) {
            this.quantityInStock = 0;
        }
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "ACTIVE";
        }
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock != null ? quantityInStock : 0;
    }

    public List<StockMovement> getStockMovements() {
        return stockMovements;
    }

    public void setStockMovements(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements;
    }

    public String getStatus() {
        return (status != null && !status.trim().isEmpty()) ? status : "ACTIVE";
    }

    public void setStatus(String status) {
        this.status = status != null ? status.toUpperCase() : "ACTIVE";
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
