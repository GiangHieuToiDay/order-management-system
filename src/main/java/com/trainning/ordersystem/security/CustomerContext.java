package com.trainning.ordersystem.security;

import com.trainning.ordersystem.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerContext {

    private final SecurityUtils securityUtils;

    public Long getCurrentCustomerId() {
        return securityUtils.getCurrentCustomerId();
    }

    public Long resolveCustomerId(Long requestedCustomerId) {
        if (securityUtils.isAdminOrStaff() && requestedCustomerId != null) {
            return requestedCustomerId;
        }
        return getCurrentCustomerId();
    }

    public boolean isAdminOrStaff() {
        return securityUtils.isAdminOrStaff();
    }

    public User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }
}