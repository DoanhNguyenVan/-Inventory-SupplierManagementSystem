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
            list = productRepository.findByProductNameContainingIgnoreCase(search.trim());
        } else {
            list = productRepository.findAllWithSupplier();
        }
        return list.stream().map(ProductDto::new).collect(Collectors.toList());
    }

    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id).map(ProductDto::new);
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
        Product saved = productRepository.save(product);
        return new ProductDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

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

        Product saved = productRepository.save(product);
        return new ProductDto(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        int currentStock = product.getQuantityInStock() != null ? product.getQuantityInStock() : 0;
        if (currentStock > 0) {
            throw new IllegalArgumentException("Cannot delete product '" + product.getProductName()
                    + "' because it currently has " + currentStock + " unit(s) in warehouse stock! Please export or zero-out stock before deleting.");
        }

        productRepository.delete(product);
    }

    public long countProducts() {
        return productRepository.count();
    }

    public long countLowStock(int threshold) {
        return productRepository.countByQuantityInStockLessThan(threshold);
    }
}
