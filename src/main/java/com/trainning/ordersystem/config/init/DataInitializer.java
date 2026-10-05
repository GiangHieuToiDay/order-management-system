package com.trainning.ordersystem.config.init;

import com.trainning.ordersystem.entity.Role;
import com.trainning.ordersystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initRoles();
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
}
