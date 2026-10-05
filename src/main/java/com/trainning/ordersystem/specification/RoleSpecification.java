package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.entity.Role;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RoleSpecification {

    private final RoleRepository roleRepository;

    public Role findRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò '" + name + "'"));
    }

    public Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò với id = " + id));
    }
}
