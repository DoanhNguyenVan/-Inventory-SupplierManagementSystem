package com.Group5.ISMS.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@SQLDelete(sql = "UPDATE suppliers SET is_deleted = 1 WHERE supplier_id = ?")
@SQLRestriction("is_deleted = 0")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "bit default 0")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "supplier")
    @JsonIgnoreProperties("supplier")
    private List<Product> products = new ArrayList<>();

    public Supplier() {
    }

    public Supplier(String supplierName, String email, String phone) {
        this.supplierName = supplierName;
        this.email = email;
        this.phone = phone;
        this.isDeleted = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
