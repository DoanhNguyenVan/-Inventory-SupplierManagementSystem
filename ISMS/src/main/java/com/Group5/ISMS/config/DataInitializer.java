package com.Group5.ISMS.config;

import com.Group5.ISMS.entity.Product;
import com.Group5.ISMS.entity.StockMovement;
import com.Group5.ISMS.entity.Supplier;
import com.Group5.ISMS.entity.User;
import com.Group5.ISMS.repository.ProductRepository;
import com.Group5.ISMS.repository.StockMovementRepository;
import com.Group5.ISMS.repository.SupplierRepository;
import com.Group5.ISMS.repository.UserRepository;
import com.Group5.ISMS.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public DataInitializer(UserRepository userRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           StockMovementRepository stockMovementRepository) {
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User(
                    "admin",
                    PasswordUtil.hashPassword("admin123"),
                    "System Administrator",
                    "admin@isms.com",
                    "ADMIN"
            );
            userRepository.save(admin);

            User staff = new User(
                    "staff",
                    PasswordUtil.hashPassword("staff123"),
                    "Warehouse Staff",
                    "staff@isms.com",
                    "STAFF"
            );
            userRepository.save(staff);
        }

        if (supplierRepository.count() == 0) {
            Supplier s1 = supplierRepository.save(new Supplier("TechLink Distribution", "contact@techlink.com", "0901234567"));
            Supplier s2 = supplierRepository.save(new Supplier("Global Hardware Co.", "sales@globalhw.com", "0912345678"));
            Supplier s3 = supplierRepository.save(new Supplier("Apex Components", "support@apexcomp.vn", "0987654321"));

            // Products always start with quantityInStock=0; stock is tracked via StockMovements only.
            Product p1 = productRepository.save(new Product(s1, "Mechanical Gaming Keyboard", 0));
            Product p2 = productRepository.save(new Product(s1, "Wireless Ergonomic Mouse", 0));
            Product p3 = productRepository.save(new Product(s2, "UltraWide 34-inch Monitor", 0));
            Product p4 = productRepository.save(new Product(s3, "USB-C Hub Multiport", 0));

            // p1: +50 -5 = 45 in stock
            stockMovementRepository.save(new StockMovement(p1, 50, LocalDateTime.now().minusDays(3)));
            stockMovementRepository.save(new StockMovement(p1, -5, LocalDateTime.now().minusDays(1)));
            p1.setQuantityInStock(45);
            productRepository.save(p1);

            // p2: +10 -2 = 8 in stock
            stockMovementRepository.save(new StockMovement(p2, 10, LocalDateTime.now().minusDays(2)));
            stockMovementRepository.save(new StockMovement(p2, -2, LocalDateTime.now().minusHours(5)));
            p2.setQuantityInStock(8);
            productRepository.save(p2);

            // p3: +20 -5 = 15 in stock
            stockMovementRepository.save(new StockMovement(p3, 20, LocalDateTime.now().minusDays(4)));
            stockMovementRepository.save(new StockMovement(p3, -5, LocalDateTime.now().minusDays(2)));
            p3.setQuantityInStock(15);
            productRepository.save(p3);

            // p4: +10 -5 = 5 in stock
            stockMovementRepository.save(new StockMovement(p4, 10, LocalDateTime.now().minusDays(1)));
            stockMovementRepository.save(new StockMovement(p4, -5, LocalDateTime.now().minusHours(2)));
            p4.setQuantityInStock(5);
            productRepository.save(p4);
        }
    }
}
