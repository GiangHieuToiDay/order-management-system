package com.trainning.ordersystem.security;

import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.security.userDetails.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    public static User getCurrentUser() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null || userDetails.getUser() == null) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Yêu cầu đăng nhập để thực hiện thao tác");
        }
        return userDetails.getUser();
    }

    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Yêu cầu đăng nhập để thực hiện thao tác");
        }
        return userDetails.getUserId();
    }

    public static Long getCurrentCustomerId() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Yêu cầu đăng nhập để thực hiện thao tác");
        }
        Long customerId = userDetails.getCustomerId();
        if (customerId == null) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND, "Tài khoản hiện tại không có thông tin khách hàng");
        }
        return customerId;
    }

    public static boolean isAuthenticated() {
        return getCurrentUserDetails() != null;
    }

    public static boolean hasRole(String role) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;
        return userDetails.hasRole(role);
    }
}
