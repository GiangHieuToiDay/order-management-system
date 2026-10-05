package com.trainning.ordersystem.mapper;

import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

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
