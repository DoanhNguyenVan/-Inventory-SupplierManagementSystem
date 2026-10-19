package com.Group5.ISMS.service;

import com.Group5.ISMS.dto.ProductDto;
import com.Group5.ISMS.entity.Product;
import com.Group5.ISMS.entity.Supplier;
import com.Group5.ISMS.repository.ProductRepository;
import com.Group5.ISMS.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final com.Group5.ISMS.repository.StockMovementRepository stockMovementRepository;

    public ProductService(ProductRepository productRepository,
                          SupplierRepository supplierRepository,
                          com.Group5.ISMS.repository.StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<ProductDto> getAllProducts(String search) {
        List<Product> list;
        if (search != null && !search.trim().isEmpty()) {
            list = productRepository.findByProductNameContainingIgnoreCaseAndIsDeletedFalse(search.trim());
        } else {
            list = productRepository.findAllActiveWithSupplier();
        }
        return list.stream().map(ProductDto::new).collect(Collectors.toList());
    }

    public List<ProductDto> getDeletedProducts(String search) {
        List<Product> list;
        if (search != null && !search.trim().isEmpty()) {
            list = productRepository.findByProductNameContainingIgnoreCaseAndIsDeletedTrue(search.trim());
        } else {
            list = productRepository.findAllDeletedWithSupplier();
        }
        return list.stream().map(ProductDto::new).collect(Collectors.toList());
    }

    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findByProductIdAndIsDeletedFalse(id).map(ProductDto::new);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required!");
        }

        String trimmedName = dto.getProductName().trim();
        if (trimmedName.length() > 150) {
            throw new IllegalArgumentException("Product name cannot exceed 150 characters!");
        }

        if (dto.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier is required for this product!");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + dto.getSupplierId()));

        int initialQuantity = 0;
        Product product = new Product(supplier, trimmedName, initialQuantity);
        product.setIsDeleted(false);
        Product saved = productRepository.save(product);
        return new ProductDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findByProductIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Active product not found with ID: " + id));

        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty!");
        }

        String trimmedName = dto.getProductName().trim();
        if (trimmedName.length() > 150) {
            throw new IllegalArgumentException("Product name cannot exceed 150 characters!");
        }

        if (dto.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier is required for this product!");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + dto.getSupplierId()));

        product.setProductName(trimmedName);
        product.setSupplier(supplier);
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            product.setStatus(dto.getStatus().trim().toUpperCase());
        }

        Product saved = productRepository.save(product);
        return new ProductDto(saved);
    }

    @Transactional
    public ProductDto toggleProductStatus(Long id, String newStatus) {
        Product product = productRepository.findByProductIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Active product not found with ID: " + id));

        if (newStatus != null && !newStatus.trim().isEmpty()) {
            String norm = newStatus.trim().toUpperCase();
            if (!norm.equals("ACTIVE") && !norm.equals("SUSPENDED")) {
                throw new IllegalArgumentException("Invalid status! Allowed values: ACTIVE, SUSPENDED");
            }
            product.setStatus(norm);
        } else {
            if ("SUSPENDED".equalsIgnoreCase(product.getStatus())) {
                product.setStatus("ACTIVE");
            } else {
                product.setStatus("SUSPENDED");
            }
        }

        Product saved = productRepository.save(product);
        return new ProductDto(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new IllegalArgumentException("Product is already in the recycle bin!");
        }

        product.setIsDeleted(true);
        product.setStatus("DISCONTINUED");
        productRepository.save(product);
    }

    @Transactional
    public ProductDto restoreProduct(Long id, Integer newQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        if (!Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new IllegalArgumentException("Product is not in the recycle bin!");
        }

        if (newQuantity != null) {
            if (newQuantity < 0) {
                throw new IllegalArgumentException("Quantity in stock cannot be negative!");
            }
            product.setQuantityInStock(newQuantity);
        }

        product.setIsDeleted(false);
        product.setStatus("ACTIVE");
        Product saved = productRepository.save(product);
        return new ProductDto(saved);
    }

    @Transactional
    public void permanentDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        if (!Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new IllegalArgumentException("Only products in the recycle bin can be permanently deleted!");
        }

        long movementCount = stockMovementRepository.countByProduct_ProductId(id);
        if (movementCount > 0) {
            throw new IllegalArgumentException("Cannot permanently delete product '" + product.getProductName()
                    + "' because it has " + movementCount + " linked stock movements. Please keep it in the recycle bin or restore it.");
        }

        productRepository.delete(product);
    }

    public long countProducts() {
        return productRepository.countByIsDeletedFalse();
    }

    public long countDeletedProducts() {
        return productRepository.countByIsDeletedTrue();
    }

    public long countLowStock(int threshold) {
        return productRepository.countByQuantityInStockLessThanAndIsDeletedFalse(threshold);
    }
}
