package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.request.role.RoleRequest;
import com.trainning.ordersystem.dto.response.role.RoleResponse;
import com.trainning.ordersystem.entity.Role;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.mapper.RoleMapper;
import com.trainning.ordersystem.repository.RoleRepository;
import com.trainning.ordersystem.repository.RoleRepositoryCustom;
import com.trainning.ordersystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleRepositoryCustom roleRepositoryCustom;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_ALREADY_EXISTS, "Vai trò '" + request.getName() + "' đã tồn tại");
        }
        Role role = roleMapper.toEntity(request);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toResponse(savedRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepositoryCustom.findAllRoleResponses();
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        return roleRepositoryCustom.findRoleResponseById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò với id = " + id));
    }

    @Override
    public RoleResponse getRoleByName(String name) {
        return roleRepositoryCustom.findRoleResponseByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò '" + name + "'"));
    }
}
