package com.trainning.ordersystem.security.userDetails;

import com.trainning.ordersystem.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Long userId;
    private final Long customerId;
    private final String roleName;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, Long customerId) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.customerId = customerId;
        this.roleName = (user != null && user.getRole() != null) ? user.getRole().getName() : null;

        List<GrantedAuthority> authList = new ArrayList<>();
        if (this.roleName != null) {
            authList.add(new SimpleGrantedAuthority("ROLE_" + this.roleName));
        }
        this.authorities = Collections.unmodifiableList(authList);
    }

    public CustomUserDetails(User user) {
        this(user, user != null && user.getCustomer() != null ? user.getCustomer().getId() : null);
    }

    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean hasRole(String role) {
        if (role == null) return false;
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(roleWithPrefix));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return user != null ? user.getEmail() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user != null && user.getStatus() != com.trainning.ordersystem.entity.enums.UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.getStatus() == com.trainning.ordersystem.entity.enums.UserStatus.ACTIVE;
    }
}
