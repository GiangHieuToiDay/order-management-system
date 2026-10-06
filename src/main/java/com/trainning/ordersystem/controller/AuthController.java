package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.request.auth.LoginRequest;
import com.trainning.ordersystem.dto.request.auth.RegisterCustomerRequest;
import com.trainning.ordersystem.dto.request.user.ChangePasswordRequest;
import com.trainning.ordersystem.dto.request.user.UpdateProfileRequest;
import com.trainning.ordersystem.dto.response.auth.AuthResponse;
import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody RegisterCustomerRequest request) {
        UserProfileResponse response = userService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký tài khoản thành công", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin tài khoản thành công", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin cá nhân thành công", response));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công", null));
    }
}
