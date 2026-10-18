package com.Group5.ISMS.repository;

import com.Group5.ISMS.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProduct_ProductId(Long productId);
    long countByProduct_ProductId(Long productId);

    @Query("SELECT sm FROM StockMovement sm JOIN FETCH sm.product LEFT JOIN FETCH sm.user ORDER BY sm.movementDate DESC")
    List<StockMovement> findAllWithProductOrderByMovementDateDesc();
}
