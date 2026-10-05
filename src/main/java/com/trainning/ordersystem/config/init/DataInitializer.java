package com.trainning.ordersystem.config.init;

import com.trainning.ordersystem.entity.Category;
import com.trainning.ordersystem.entity.Product;
import com.trainning.ordersystem.entity.Role;
import com.trainning.ordersystem.entity.enums.CategoryStatus;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import com.trainning.ordersystem.repository.CategoryRepository;
import com.trainning.ordersystem.repository.ProductRepository;
import com.trainning.ordersystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        initRoles();
        initCategoriesAndProducts();
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
                createProduct("iPhone 15 Pro Max 256GB", "Flagship Apple màn hình OLED 120Hz chip A17 Pro", "IP15PM-256", new BigDecimal("29990000"), "Chiếc", 50, ProductStatus.ACTIVE, electronics),
                createProduct("Samsung Galaxy S24 Ultra 256GB", "Điện thoại Samsung AI cao cấp camera 200MP", "SS-S24U-256", new BigDecimal("26990000"), "Chiếc", 40, ProductStatus.ACTIVE, electronics),
                createProduct("Xiaomi 14 Ultra 512GB", "Camera Leica cảm biến 1 inch đỉnh cao", "MI-14U-512", new BigDecimal("21490000"), "Chiếc", 30, ProductStatus.ACTIVE, electronics),
                createProduct("OPPO Find X7 Ultra", "Flagship camera kép tiềm vọng Hasselblad", "OPPO-X7U", new BigDecimal("19900000"), "Chiếc", 25, ProductStatus.ACTIVE, electronics),
                createProduct("Củ sạc nhanh Anker 65W GaN", "Củ sạc 3 cổng công nghệ GaN nhỏ gọn", "ANKER-65W", new BigDecimal("650000"), "Cái", 150, ProductStatus.ACTIVE, electronics),

                // 6-10: Laptop & Máy tính
                createProduct("MacBook Pro 14 M3 Pro", "Laptop Apple chip M3 Pro 18GB RAM 512GB SSD", "MBP14-M3P", new BigDecimal("48990000"), "Chiếc", 15, ProductStatus.ACTIVE, laptops),
                createProduct("Dell XPS 13 Plus 9320", "Laptop văn phòng mỏng nhẹ màn hình cảm ứng OLED", "DELL-XPS13", new BigDecimal("37500000"), "Chiếc", 20, ProductStatus.ACTIVE, laptops),
                createProduct("Lenovo ThinkPad X1 Carbon Gen 11", "Laptop doanh nhân siêu bền nhẹ chuẩn quân đội", "TP-X1C11", new BigDecimal("41900000"), "Chiếc", 12, ProductStatus.ACTIVE, laptops),
                createProduct("Asus ROG Zephyrus G14", "Laptop gaming mỏng nhẹ màn hình Nebula HDR", "ROG-G14", new BigDecimal("35900000"), "Chiếc", 18, ProductStatus.ACTIVE, laptops),
                createProduct("Màn hình LG UltraFine 27 inch 4K", "Màn hình chuyên đồ họa IPS 4K HDR400", "LG-27UP850", new BigDecimal("8900000"), "Chiếc", 35, ProductStatus.ACTIVE, laptops),

                // 11-15: Thiết bị âm thanh
                createProduct("Tai nghe Apple AirPods Pro 2 USB-C", "Tai nghe True Wireless chống ồn chủ động", "APP2-USBC", new BigDecimal("5590000"), "Chiếc", 80, ProductStatus.ACTIVE, audio),
                createProduct("Tai nghe Sony WH-1000XM5", "Tai nghe over-ear chống ồn hàng đầu thị trường", "SONY-XM5", new BigDecimal("7290000"), "Chiếc", 45, ProductStatus.ACTIVE, audio),
                createProduct("Loa Bluetooth Marshall Stanmore III", "Loa nghe nhạc phòng khách âm thanh vintage", "MARSHALL-ST3", new BigDecimal("8990000"), "Chiếc", 20, ProductStatus.ACTIVE, audio),
                createProduct("Loa JBL Charge 5", "Loa bluetooth kháng nước kháng bụi IP67", "JBL-CHARGE5", new BigDecimal("3390000"), "Chiếc", 60, ProductStatus.ACTIVE, audio),
                createProduct("Tai nghe Bose QuietComfort Ultra", "Tai nghe chụp tai công nghệ Bose Immersive Audio", "BOSE-QCU", new BigDecimal("8490000"), "Chiếc", 30, ProductStatus.ACTIVE, audio),

                // 16-20: Đồng hồ thông minh
                createProduct("Apple Watch Series 9 GPS 45mm", "Đồng hồ đo nhịp tim, ECG và thể thao", "AW-S9-45", new BigDecimal("9790000"), "Chiếc", 40, ProductStatus.ACTIVE, smartwatches),
                createProduct("Samsung Galaxy Watch 6 Classic", "Đồng hồ mặt tròn viền xoay vật lý đẳng cấp", "GW-6C", new BigDecimal("6890000"), "Chiếc", 35, ProductStatus.ACTIVE, smartwatches),
                createProduct("Garmin Fenix 7 Pro Solar", "Đồng hồ chạy bộ leo núi pin năng lượng mặt trời", "GARMIN-F7P", new BigDecimal("18990000"), "Chiếc", 15, ProductStatus.ACTIVE, smartwatches),
                createProduct("Vòng đeo tay Xiaomi Band 8", "Vòng tay theo dõi bước chân và giấc ngủ", "MIBAND-8", new BigDecimal("790000"), "Chiếc", 100, ProductStatus.INACTIVE, smartwatches),
                createProduct("Apple Watch Ultra 2 GPS + Cellular", "Đồng hồ thể thao mạo hiểm vỏ Titan 49mm", "AW-ULTRA2", new BigDecimal("20990000"), "Chiếc", 10, ProductStatus.INACTIVE, smartwatches)
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
                                  ProductStatus status, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setSku(sku);
        product.setPrice(price);
        product.setUnit(unit);
        product.setStockQuantity(stock);
        product.setStatus(status);
        product.setCategory(category);
        return product;
    }
}
