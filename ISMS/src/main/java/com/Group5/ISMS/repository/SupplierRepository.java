package com.Group5.ISMS.repository;

import com.Group5.ISMS.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findBySupplierNameContainingIgnoreCase(String name);
}
