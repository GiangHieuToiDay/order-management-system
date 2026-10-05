package com.trainning.ordersystem.mapper;

import com.trainning.ordersystem.dto.request.role.RoleRequest;
import com.trainning.ordersystem.dto.response.role.RoleResponse;
import com.trainning.ordersystem.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);
}
