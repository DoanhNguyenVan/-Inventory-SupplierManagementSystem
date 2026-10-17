package com.Group5.ISMS.dto;

import com.Group5.ISMS.entity.Supplier;

public class SupplierDto {

    private Long supplierId;
    private String supplierName;
    private String email;
    private String phone;
    private int productCount;

    public SupplierDto() {
    }

    public SupplierDto(Supplier s) {
        if (s != null) {
            this.supplierId = s.getSupplierId();
            this.supplierName = s.getSupplierName();
            this.email = s.getEmail();
            this.phone = s.getPhone();
            this.productCount = s.getProducts() != null ? s.getProducts().size() : 0;
        }
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
}
