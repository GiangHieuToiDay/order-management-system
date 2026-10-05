package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.request.role.RoleRequest;
import com.trainning.ordersystem.dto.response.role.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse createRole(RoleRequest request);
    List<RoleResponse> getAllRoles();
    RoleResponse getRoleById(Long id);
    RoleResponse getRoleByName(String name);
}
