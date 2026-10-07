package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.user.CreateUserRequest;
import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.entity.enums.MembershipLevel;
import com.trainning.ordersystem.entity.enums.UserStatus;
import com.trainning.ordersystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserProfileResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo người dùng thành công", response));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) UserStatus status) {
        PageResponse<UserProfileResponse> response = userService.getUsers(page, size, keyword, roleId, status);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách người dùng thành công", response));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        UserProfileResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết người dùng thành công", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái người dùng thành công", null));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCustomerById(@PathVariable Long customerId) {
        UserProfileResponse response = userService.getCustomerById(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin khách hàng thành công", response));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PatchMapping("/customers/{customerId}/membership")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCustomerMembership(
            @PathVariable Long customerId,
            @RequestParam MembershipLevel level) {
        UserProfileResponse response = userService.updateCustomerMembership(customerId, level);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hạng thành viên thành công", response));
    }
}
