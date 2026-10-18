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

    private void validateSupplierData(String name, String email, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Supplier name cannot exceed 100 characters");
        }
        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim();
            if (trimmedEmail.length() > 100) {
                throw new IllegalArgumentException("Supplier email cannot exceed 100 characters");
            }
            if (!trimmedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("Invalid supplier email format");
            }
        }
        if (phone != null && !phone.trim().isEmpty()) {
            String trimmedPhone = phone.trim();
            if (trimmedPhone.length() > 20) {
                throw new IllegalArgumentException("Supplier phone number cannot exceed 20 characters");
            }
            if (!trimmedPhone.matches("^[+0-9\\-\\s().]{4,20}$")) {
                throw new IllegalArgumentException("Invalid supplier phone number format");
            }
        }
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto) {
        validateSupplierData(dto.getSupplierName(), dto.getEmail(), dto.getPhone());

        Supplier supplier = new Supplier(
                dto.getSupplierName().trim(),
                dto.getEmail() != null && !dto.getEmail().trim().isEmpty() ? dto.getEmail().trim() : null,
                dto.getPhone() != null && !dto.getPhone().trim().isEmpty() ? dto.getPhone().trim() : null
        );
        Supplier saved = supplierRepository.save(supplier);
        return new SupplierDto(saved);
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + id));

        validateSupplierData(dto.getSupplierName(), dto.getEmail(), dto.getPhone());

        supplier.setSupplierName(dto.getSupplierName().trim());
        supplier.setEmail(dto.getEmail() != null && !dto.getEmail().trim().isEmpty() ? dto.getEmail().trim() : null);
        supplier.setPhone(dto.getPhone() != null && !dto.getPhone().trim().isEmpty() ? dto.getPhone().trim() : null);

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
