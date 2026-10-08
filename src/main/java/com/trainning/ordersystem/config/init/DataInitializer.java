package com.trainning.ordersystem.config.init;

import com.trainning.ordersystem.entity.Category;
import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.Product;
import com.trainning.ordersystem.entity.Role;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.CategoryStatus;
import com.trainning.ordersystem.entity.enums.MembershipLevel;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import com.trainning.ordersystem.entity.enums.UserStatus;
import com.trainning.ordersystem.repository.CategoryRepository;
import com.trainning.ordersystem.repository.CustomerRepository;
import com.trainning.ordersystem.repository.ProductRepository;
import com.trainning.ordersystem.repository.RoleRepository;
import com.trainning.ordersystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.trainning.ordersystem.service.RedisService;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final RedisService redisService;

    @Override
    public void run(String... args) {
        fixDatabaseUnicodeSchemaAndData();
        initRoles();
        initDefaultUsers();
        initCategoriesAndProducts();
        initSampleCustomer();
        warmUpStockToRedis(); // [DECOUPLED STOCK CACHING]
    }

    private void warmUpStockToRedis() {
        // // [DECOUPLED STOCK CACHING] Nạp trước (warm-up) số lượng tồn kho của toàn bộ sản phẩm lên Redis
        try {
            if (redisService != null) {
                productRepository.findAll().forEach(p -> {
                    if (p.getStockQuantity() != null) {
                        redisService.setStock(p.getId(), p.getStockQuantity());
                    }
                });
                log.info(">> [DECOUPLED STOCK CACHING] Đã warm-up tồn kho tất cả sản phẩm lên Redis thành công.");
            }
        } catch (Exception e) {
            log.warn(">> [DECOUPLED STOCK CACHING] Lỗi khi warm-up tồn kho lên Redis: {}", e.getMessage());
        }
    }

    private void initRoles() {
        initRoleIfNotExists("CUSTOMER", "Khách hàng — xem và mua sắm sản phẩm");
        initRoleIfNotExists("STAFF", "Nhân viên — quản lý sản phẩm, đơn hàng và kho");
        initRoleIfNotExists("ADMIN", "Quản trị viên — toàn quyền quản trị hệ thống");
    }

    private void initRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
            log.info(">> [DataInitializer] Đã khởi tạo vai trò mặc định: {}", roleName);
        }
    }

    private void initCategoriesAndProducts() {
        if (productRepository.count() > 0) {
            log.info(">> [DataInitializer] Dữ liệu sản phẩm đã tồn tại ({} sản phẩm), bỏ qua khởi tạo.", productRepository.count());
            return;
        }

        // Tạo categories nếu chưa có
        Category electronics = getOrCreateCategory("Điện thoại & Phụ kiện", "Các thiết bị di động và phụ kiện công nghệ");
        Category laptops = getOrCreateCategory("Laptop & Máy tính", "Máy tính xách tay và thiết bị văn phòng");
        Category audio = getOrCreateCategory("Thiết bị âm thanh", "Tai nghe, loa bluetooth cao cấp");
        Category smartwatches = getOrCreateCategory("Đồng hồ thông minh", "Đồng hồ và vòng đeo tay thể thao");

        // Tạo 20 sản phẩm mẫu (4 danh mục, mỗi danh mục 5 sản phẩm)
        List<Product> products = List.of(
                // 1-5: Điện thoại & Phụ kiện
                createProduct("iPhone 15 Pro Max 256GB", "Flagship Apple màn hình OLED 120Hz chip A17 Pro", "IP15PM-256", new BigDecimal("29990000"), "Chiếc", 50, ProductStatus.ACTIVE, electronics, "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&auto=format&fit=crop&q=80"),
                createProduct("Samsung Galaxy S24 Ultra 256GB", "Điện thoại Samsung AI cao cấp camera 200MP", "SS-S24U-256", new BigDecimal("26990000"), "Chiếc", 40, ProductStatus.ACTIVE, electronics, "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600&auto=format&fit=crop&q=80"),
                createProduct("Xiaomi 14 Ultra 512GB", "Camera Leica cảm biến 1 inch đỉnh cao", "MI-14U-512", new BigDecimal("21490000"), "Chiếc", 30, ProductStatus.ACTIVE, electronics, "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600&auto=format&fit=crop&q=80"),
                createProduct("OPPO Find X7 Ultra", "Flagship camera kép tiềm vọng Hasselblad", "OPPO-X7U", new BigDecimal("19900000"), "Chiếc", 25, ProductStatus.ACTIVE, electronics, "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=600&auto=format&fit=crop&q=80"),
                createProduct("Củ sạc nhanh Anker 65W GaN", "Củ sạc 3 cổng công nghệ GaN nhỏ gọn", "ANKER-65W", new BigDecimal("650000"), "Cái", 150, ProductStatus.ACTIVE, electronics, "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=600&auto=format&fit=crop&q=80"),

                // 6-10: Laptop & Máy tính
                createProduct("MacBook Pro 14 M3 Pro", "Laptop Apple chip M3 Pro 18GB RAM 512GB SSD", "MBP14-M3P", new BigDecimal("48990000"), "Chiếc", 15, ProductStatus.ACTIVE, laptops, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80"),
                createProduct("Dell XPS 13 Plus 9320", "Laptop văn phòng mỏng nhẹ màn hình cảm ứng OLED", "DELL-XPS13", new BigDecimal("37500000"), "Chiếc", 20, ProductStatus.ACTIVE, laptops, "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=600&auto=format&fit=crop&q=80"),
                createProduct("Lenovo ThinkPad X1 Carbon Gen 11", "Laptop doanh nhân siêu bền nhẹ chuẩn quân đội", "TP-X1C11", new BigDecimal("41900000"), "Chiếc", 12, ProductStatus.ACTIVE, laptops, "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600&auto=format&fit=crop&q=80"),
                createProduct("Asus ROG Zephyrus G14", "Laptop gaming mỏng nhẹ màn hình Nebula HDR", "ROG-G14", new BigDecimal("35900000"), "Chiếc", 18, ProductStatus.ACTIVE, laptops, "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=600&auto=format&fit=crop&q=80"),
                createProduct("Màn hình LG UltraFine 27 inch 4K", "Màn hình chuyên đồ họa IPS 4K HDR400", "LG-27UP850", new BigDecimal("8900000"), "Chiếc", 35, ProductStatus.ACTIVE, laptops, "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop&q=80"),

                // 11-15: Thiết bị âm thanh
                createProduct("Tai nghe Apple AirPods Pro 2 USB-C", "Tai nghe True Wireless chống ồn chủ động", "APP2-USBC", new BigDecimal("5590000"), "Chiếc", 80, ProductStatus.ACTIVE, audio, "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=600&auto=format&fit=crop&q=80"),
                createProduct("Tai nghe Sony WH-1000XM5", "Tai nghe over-ear chống ồn hàng đầu thị trường", "SONY-XM5", new BigDecimal("7290000"), "Chiếc", 45, ProductStatus.ACTIVE, audio, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80"),
                createProduct("Loa Bluetooth Marshall Stanmore III", "Loa nghe nhạc phòng khách âm thanh vintage", "MARSHALL-ST3", new BigDecimal("8990000"), "Chiếc", 20, ProductStatus.ACTIVE, audio, "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=600&auto=format&fit=crop&q=80"),
                createProduct("Loa JBL Charge 5", "Loa bluetooth kháng nước kháng bụi IP67", "JBL-CHARGE5", new BigDecimal("3390000"), "Chiếc", 60, ProductStatus.ACTIVE, audio, "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600&auto=format&fit=crop&q=80"),
                createProduct("Tai nghe Bose QuietComfort Ultra", "Tai nghe chụp tai công nghệ Bose Immersive Audio", "BOSE-QCU", new BigDecimal("8490000"), "Chiếc", 30, ProductStatus.ACTIVE, audio, "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&auto=format&fit=crop&q=80"),

                // 16-20: Đồng hồ thông minh
                createProduct("Apple Watch Series 9 GPS 45mm", "Đồng hồ đo nhịp tim, ECG và thể thao", "AW-S9-45", new BigDecimal("9790000"), "Chiếc", 40, ProductStatus.ACTIVE, smartwatches, "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600&auto=format&fit=crop&q=80"),
                createProduct("Samsung Galaxy Watch 6 Classic", "Đồng hồ mặt tròn viền xoay vật lý đẳng cấp", "GW-6C", new BigDecimal("6890000"), "Chiếc", 35, ProductStatus.ACTIVE, smartwatches, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80"),
                createProduct("Garmin Fenix 7 Pro Solar", "Đồng hồ chạy bộ leo núi pin năng lượng mặt trời", "GARMIN-F7P", new BigDecimal("18990000"), "Chiếc", 15, ProductStatus.ACTIVE, smartwatches, "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&auto=format&fit=crop&q=80"),
                createProduct("Vòng đeo tay Xiaomi Band 8", "Vòng tay theo dõi bước chân và giấc ngủ", "MIBAND-8", new BigDecimal("790000"), "Chiếc", 100, ProductStatus.INACTIVE, smartwatches, "https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=600&auto=format&fit=crop&q=80"),
                createProduct("Apple Watch Ultra 2 GPS + Cellular", "Đồng hồ thể thao mạo hiểm vỏ Titan 49mm", "AW-ULTRA2", new BigDecimal("20990000"), "Chiếc", 10, ProductStatus.INACTIVE, smartwatches, "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=600&auto=format&fit=crop&q=80")
        );

        productRepository.saveAll(products);
        log.info(">> [DataInitializer] Đã khởi tạo thành công {} sản phẩm mẫu cho 4 danh mục!", products.size());
    }

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            category.setStatus(CategoryStatus.ACTIVE);
            Category saved = categoryRepository.save(category);
            log.info(">> [DataInitializer] Đã khởi tạo danh mục: {}", name);
            return saved;
        });
    }

    private Product createProduct(String name, String description, String sku,
                                  BigDecimal price, String unit, Integer stock,
                                  ProductStatus status, Category category, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setSku(sku);
        product.setPrice(price);
        product.setUnit(unit);
        product.setStockQuantity(stock);
        product.setStatus(status);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        return product;
    }

    private void initDefaultUsers() {
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole != null && !userRepository.existsByEmail("admin@gmail.com")) {
            User admin = new User();
            admin.setRole(adminRole);
            admin.setFullName("Quản trị viên Hệ thống");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setPhone("0900000001");
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
            log.info(">> [DataInitializer] Đã khởi tạo tài khoản ADMIN mẫu: admin@gmail.com / 123456");
        }

        Role staffRole = roleRepository.findByName("STAFF").orElse(null);
        if (staffRole != null && !userRepository.existsByEmail("staff@gmail.com")) {
            User staff = new User();
            staff.setRole(staffRole);
            staff.setFullName("Nhân viên Bán hàng");
            staff.setEmail("staff@gmail.com");
            staff.setPassword(passwordEncoder.encode("123456"));
            staff.setPhone("0900000002");
            staff.setStatus(UserStatus.ACTIVE);
            userRepository.save(staff);
            log.info(">> [DataInitializer] Đã khởi tạo tài khoản STAFF mẫu: staff@gmail.com / 123456");
        }
    }

    private void initSampleCustomer() {
        Role customerRole = roleRepository.findByName("CUSTOMER").orElse(null);
        if (customerRole == null) {
            return;
        }

        if (!userRepository.existsByEmail("customer1@gmail.com")) {
            User user = new User();
            user.setRole(customerRole);
            user.setFullName("Nguyễn Văn A");
            user.setEmail("customer1@gmail.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setPhone("0987654321");
            user.setStatus(UserStatus.ACTIVE);
            User savedUser = userRepository.save(user);

            Customer customer = new Customer();
            customer.setUser(savedUser);
            customer.setAddress("Hà Nội");
            customer.setMembershipLevel(MembershipLevel.REGULAR);
            customerRepository.save(customer);

            log.info(">> [DataInitializer] Đã khởi tạo khách hàng mẫu thành công (customer1@gmail.com / 123456)");
        } else {
            // Cập nhật lại mật khẩu sang BCrypt nếu tài khoản cũ đang lưu plain text
            userRepository.findByEmail("customer1@gmail.com").ifPresent(user -> {
                if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(user);
                    log.info(">> [DataInitializer] Đã mã hóa lại mật khẩu BCrypt cho user customer1@gmail.com");
                }
            });
        }
    }

    private void fixDatabaseUnicodeSchemaAndData() {
        try {
            // Đảm bảo các cột văn bản tiếng Việt là NVARCHAR trong SQL Server
            String[] alterSqls = {
                "ALTER TABLE categories ALTER COLUMN name NVARCHAR(100) NOT NULL",
                "ALTER TABLE categories ALTER COLUMN description NVARCHAR(500)",
                "ALTER TABLE products ALTER COLUMN name NVARCHAR(200) NOT NULL",
                "ALTER TABLE products ALTER COLUMN unit NVARCHAR(50)",
                "ALTER TABLE users ALTER COLUMN full_name NVARCHAR(100) NOT NULL",
                "ALTER TABLE customers ALTER COLUMN address NVARCHAR(500)",
                "ALTER TABLE orders ALTER COLUMN shipping_address NVARCHAR(500) NOT NULL"
            };
            for (String sql : alterSqls) {
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception e) {
                    log.debug("Bỏ qua alter column: {}", e.getMessage());
                }
            }

            // Sửa lại dữ liệu tiếng Việt nếu trước đó bị lưu thành dấu hỏi chấm do VARCHAR
            jdbcTemplate.update("UPDATE categories SET name = N'Đồng hồ thông minh', description = N'Đồng hồ và vòng đeo tay thể thao' WHERE name LIKE N'%ng h% th%ng minh%' OR name LIKE N'%?%'");
            jdbcTemplate.update("UPDATE categories SET name = N'Thiết bị âm thanh', description = N'Tai nghe, loa bluetooth cao cấp' WHERE name LIKE N'%t b% âm thanh%' OR name LIKE N'%t b% %m thanh%'");
            jdbcTemplate.update("UPDATE categories SET name = N'Điện thoại & Phụ kiện', description = N'Các thiết bị di động và phụ kiện công nghệ' WHERE name LIKE N'%i%n tho%i%'");
            jdbcTemplate.update("UPDATE categories SET name = N'Laptop & Máy tính', description = N'Máy tính xách tay và thiết bị văn phòng' WHERE name LIKE N'%M%y t%nh%'");

            jdbcTemplate.update("UPDATE products SET unit = N'Chiếc' WHERE unit LIKE N'%Chi%c%' OR unit LIKE N'%?%'");
            jdbcTemplate.update("UPDATE products SET unit = N'Cái' WHERE unit LIKE N'%C%i%'");

            // Xóa cache Redis để phản ánh dữ liệu mới
            try {
                if (redisService != null) {
                    redisService.clearCachePattern("product:list:*");

                    // // [DECOUPLED STOCK CACHING] Nạp trước (warm-up) số lượng tồn kho của toàn bộ sản phẩm lên Redis
                    productRepository.findAll().forEach(p -> {
                        if (p.getStockQuantity() != null) {
                            redisService.setStock(p.getId(), p.getStockQuantity());
                        }
                    });
                    log.info(">> [DECOUPLED STOCK CACHING] Đã nạp trước (warm-up) số lượng tồn kho của tất cả sản phẩm lên Redis.");
                }
            } catch (Exception ignored) {}

            log.info(">> [DataInitializer] Đã kiểm tra và đồng bộ bảng mã Unicode tiếng Việt cho database.");
        } catch (Exception e) {
            log.warn(">> [DataInitializer] Lỗi khi cập nhật bảng mã Unicode: {}", e.getMessage());
        }
    }
}
