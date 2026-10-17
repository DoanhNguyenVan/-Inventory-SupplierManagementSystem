package com.Group5.ISMS.service;

import com.Group5.ISMS.dto.ProductDto;
import com.Group5.ISMS.dto.SupplierDto;
import com.Group5.ISMS.entity.Supplier;
import com.Group5.ISMS.repository.ProductRepository;
import com.Group5.ISMS.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public SupplierService(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    public List<SupplierDto> getAllSuppliers(String search) {
        List<Supplier> list;
        if (search != null && !search.trim().isEmpty()) {
            list = supplierRepository.findBySupplierNameContainingIgnoreCase(search.trim());
        } else {
            list = supplierRepository.findAll();
        }
        return list.stream().map(SupplierDto::new).collect(Collectors.toList());
    }

    public Optional<SupplierDto> getSupplierById(Long id) {
        return supplierRepository.findById(id).map(SupplierDto::new);
    }

    public List<ProductDto> getProductsBySupplierId(Long supplierId) {
        return productRepository.findBySupplier_SupplierId(supplierId)
                .stream()
                .map(ProductDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto) {
        if (dto.getSupplierName() == null || dto.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        Supplier supplier = new Supplier(
                dto.getSupplierName().trim(),
                dto.getEmail() != null ? dto.getEmail().trim() : null,
                dto.getPhone() != null ? dto.getPhone().trim() : null
        );
        Supplier saved = supplierRepository.save(supplier);
        return new SupplierDto(saved);
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + id));

        if (dto.getSupplierName() == null || dto.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be empty");
        }

        supplier.setSupplierName(dto.getSupplierName().trim());
        supplier.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        supplier.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);

        Supplier saved = supplierRepository.save(supplier);
        return new SupplierDto(saved);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + id));

        long count = productRepository.countBySupplier_SupplierId(id);
        if (count > 0) {
            throw new IllegalArgumentException("Cannot delete supplier '" + supplier.getSupplierName()
                    + "' because it has " + count + " linked product(s) in inventory! Please reassign or delete the related products first.");
        }

        supplierRepository.delete(supplier);
    }

    public long countSuppliers() {
        return supplierRepository.count();
    }
}
