package com.Group5.ISMS.service;

import com.Group5.ISMS.dto.StockMovementDto;
import com.Group5.ISMS.entity.Product;
import com.Group5.ISMS.entity.StockMovement;
import com.Group5.ISMS.entity.User;
import com.Group5.ISMS.repository.ProductRepository;
import com.Group5.ISMS.repository.StockMovementRepository;
import com.Group5.ISMS.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<StockMovementDto> getAllStockMovements() {
        return stockMovementRepository.findAllWithProductOrderByMovementDateDesc()
                .stream()
                .map(StockMovementDto::new)
                .collect(Collectors.toList());
    }

    public Optional<StockMovementDto> getStockMovementById(Long id) {
        return stockMovementRepository.findById(id).map(StockMovementDto::new);
    }

    public List<StockMovementDto> getStockMovementsByProduct(Long productId) {
        return stockMovementRepository.findByProduct_ProductId(productId)
                .stream()
                .map(StockMovementDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockMovementDto recordStockMovement(StockMovementDto dto, Long currentUserId) {
        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("Please select a product!");
        }
        if (dto.getQuantityChange() == null || dto.getQuantityChange() == 0) {
            throw new IllegalArgumentException("Quantity change cannot be zero!");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

        int currentStock = product.getQuantityInStock() != null ? product.getQuantityInStock() : 0;
        int newStock = currentStock + dto.getQuantityChange();

        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock in warehouse! Product '" + product.getProductName()
                            + "' currently only has " + currentStock + " units available, cannot export " + Math.abs(dto.getQuantityChange()) + " units."
            );
        }

        product.setQuantityInStock(newStock);
        productRepository.save(product);

        User user = null;
        if (currentUserId != null) {
            user = userRepository.findById(currentUserId).orElse(null);
        }

        LocalDateTime date = dto.getMovementDate() != null ? dto.getMovementDate() : LocalDateTime.now();
        StockMovement movement = new StockMovement(product, dto.getQuantityChange(), date, user);
        StockMovement saved = stockMovementRepository.save(movement);

        return new StockMovementDto(saved);
    }

    @Transactional
    public void deleteStockMovement(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock movement record not found with ID: " + id));

        Product product = movement.getProduct();
        if (product != null) {
            int revertedStock = (product.getQuantityInStock() != null ? product.getQuantityInStock() : 0) - movement.getQuantityChange();
            if (revertedStock >= 0) {
                product.setQuantityInStock(revertedStock);
                productRepository.save(product);
            }
        }

        stockMovementRepository.delete(movement);
    }

    public long countMovements() {
        return stockMovementRepository.count();
    }
}
