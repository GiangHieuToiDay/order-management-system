package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.role.RoleResponse;

import java.util.List;
import java.util.Optional;

public interface RoleRepositoryCustom {

    List<RoleResponse> findAllRoleResponses();

    Optional<RoleResponse> findRoleResponseById(Long id);

    Optional<RoleResponse> findRoleResponseByName(String name);
}
