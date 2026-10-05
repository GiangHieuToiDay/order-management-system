package com.trainning.ordersystem.dto.response.auth;

import com.trainning.ordersystem.entity.enums.MembershipLevel;
import com.trainning.ordersystem.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private UserStatus status;
    private LocalDateTime createdAt;

    // Các trường đặc thù của Customer (null nếu là Staff / Admin)
    private Long customerId;
    private String address;
    private MembershipLevel membershipLevel;
    private BigDecimal totalSpent;
}
