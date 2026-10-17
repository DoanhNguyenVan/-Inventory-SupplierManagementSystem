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
        // Seed default admin if user table empty
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
            System.out.println(">>> Initialized default users: admin/admin123 and staff/staff123");
        }

        // Seed suppliers if empty
        if (supplierRepository.count() == 0) {
            Supplier s1 = supplierRepository.save(new Supplier("TechLink Distribution", "contact@techlink.com", "0901234567"));
            Supplier s2 = supplierRepository.save(new Supplier("Global Hardware Co.", "sales@globalhw.com", "0912345678"));
            Supplier s3 = supplierRepository.save(new Supplier("Apex Components", "support@apexcomp.vn", "0987654321"));

            Product p1 = productRepository.save(new Product(s1, "Mechanical Gaming Keyboard", 45));
            Product p2 = productRepository.save(new Product(s1, "Wireless Ergonomic Mouse", 8)); // Low stock (<10)
            Product p3 = productRepository.save(new Product(s2, "UltraWide 34-inch Monitor", 15));
            Product p4 = productRepository.save(new Product(s3, "USB-C Hub Multiport", 5)); // Low stock (<10)

            stockMovementRepository.save(new StockMovement(p1, 50, LocalDateTime.now().minusDays(3)));
            stockMovementRepository.save(new StockMovement(p1, -5, LocalDateTime.now().minusDays(1)));
            stockMovementRepository.save(new StockMovement(p2, 10, LocalDateTime.now().minusDays(2)));
            stockMovementRepository.save(new StockMovement(p2, -2, LocalDateTime.now().minusHours(5)));
            stockMovementRepository.save(new StockMovement(p3, 20, LocalDateTime.now().minusDays(4)));
            stockMovementRepository.save(new StockMovement(p3, -5, LocalDateTime.now().minusDays(2)));
            stockMovementRepository.save(new StockMovement(p4, 10, LocalDateTime.now().minusDays(1)));
            stockMovementRepository.save(new StockMovement(p4, -5, LocalDateTime.now().minusHours(2)));

            System.out.println(">>> Initialized starter suppliers, products, and stock movements");
        }
    }
}
