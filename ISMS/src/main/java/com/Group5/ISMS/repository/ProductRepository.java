package com.Group5.ISMS.repository;

import com.Group5.ISMS.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductNameContainingIgnoreCaseAndIsDeletedFalse(String name);

    List<Product> findByProductNameContainingIgnoreCaseAndIsDeletedTrue(String name);

    long countByQuantityInStockLessThanAndIsDeletedFalse(int threshold);

    long countBySupplier_SupplierIdAndIsDeletedFalse(Long supplierId);

    List<Product> findBySupplier_SupplierIdAndIsDeletedFalse(Long supplierId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier WHERE p.isDeleted = false ORDER BY p.productId ASC")
    List<Product> findAllActiveWithSupplier();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier WHERE p.isDeleted = true ORDER BY p.productId ASC")
    List<Product> findAllDeletedWithSupplier();

    long countByIsDeletedFalse();

    long countByIsDeletedTrue();

    Optional<Product> findByProductIdAndIsDeletedFalse(Long id);
}
