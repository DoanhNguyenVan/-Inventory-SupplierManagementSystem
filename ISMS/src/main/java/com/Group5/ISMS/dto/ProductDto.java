package com.Group5.ISMS.dto;

import com.Group5.ISMS.entity.Product;

public class ProductDto {

    private Long productId;
    private Long supplierId;
    private String supplierName;
    private String productName;
    private Integer quantityInStock;

    public ProductDto() {
    }

    public ProductDto(Product p) {
        if (p != null) {
            this.productId = p.getProductId();
            if (p.getSupplier() != null) {
                this.supplierId = p.getSupplier().getSupplierId();
                this.supplierName = p.getSupplier().getSupplierName();
            }
            this.productName = p.getProductName();
            this.quantityInStock = p.getQuantityInStock();
        }
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
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
        this.quantityInStock = quantityInStock;
    }
}
