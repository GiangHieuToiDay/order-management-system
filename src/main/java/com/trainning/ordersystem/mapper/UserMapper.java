package com.trainning.ordersystem.mapper;

import com.trainning.ordersystem.dto.request.auth.RegisterCustomerRequest;
import com.trainning.ordersystem.dto.request.user.CreateUserRequest;
import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterCustomerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "membershipLevel", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    UserProfileResponse toProfileResponse(User user);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.address", target = "address")
    @Mapping(source = "customer.membershipLevel", target = "membershipLevel")
    @Mapping(target = "totalSpent", ignore = true)
    void enrichWithCustomer(Customer customer, @MappingTarget UserProfileResponse response);
}
