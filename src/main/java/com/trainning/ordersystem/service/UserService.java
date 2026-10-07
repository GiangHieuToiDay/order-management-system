package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.auth.LoginRequest;
import com.trainning.ordersystem.dto.request.auth.RegisterCustomerRequest;
import com.trainning.ordersystem.dto.request.user.ChangePasswordRequest;
import com.trainning.ordersystem.dto.request.user.CreateUserRequest;
import com.trainning.ordersystem.dto.request.user.UpdateProfileRequest;
import com.trainning.ordersystem.dto.response.auth.AuthResponse;
import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.entity.enums.MembershipLevel;
import com.trainning.ordersystem.entity.enums.UserStatus;

public interface UserService {

    UserProfileResponse registerCustomer(RegisterCustomerRequest request);


    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String authHeader);

    UserProfileResponse getMyProfile();


    UserProfileResponse updateMyProfile(UpdateProfileRequest request);


    void changePassword(ChangePasswordRequest request);


    UserProfileResponse createUser(CreateUserRequest request);


    UserProfileResponse getUserById(Long userId);


    PageResponse<UserProfileResponse> getUsers(int page, int size, String keyword, Long roleId, UserStatus status);


    void updateUserStatus(Long userId, UserStatus status);


    UserProfileResponse getCustomerById(Long customerId);


    UserProfileResponse updateCustomerMembership(Long customerId, MembershipLevel membershipLevel);
}
