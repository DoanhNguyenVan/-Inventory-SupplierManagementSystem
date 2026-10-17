package com.Group5.ISMS.repository;

import com.Group5.ISMS.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductNameContainingIgnoreCase(String name);

    long countByQuantityInStockLessThan(int threshold);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier")
    List<Product> findAllWithSupplier();
}
