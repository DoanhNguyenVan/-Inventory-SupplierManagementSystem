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

    public ProductService(ProductRepository productRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
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

        if (dto.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier is required for this product!");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + dto.getSupplierId()));

        int initialQuantity = 0;
        Product product = new Product(supplier, dto.getProductName().trim(), initialQuantity);
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

        if (dto.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier is required for this product!");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + dto.getSupplierId()));

        product.setProductName(dto.getProductName().trim());
        product.setSupplier(supplier);

        Product saved = productRepository.save(product);
        return new ProductDto(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        productRepository.delete(product);
    }

    public long countProducts() {
        return productRepository.count();
    }

    public long countLowStock(int threshold) {
        return productRepository.countByQuantityInStockLessThan(threshold);
    }
}
