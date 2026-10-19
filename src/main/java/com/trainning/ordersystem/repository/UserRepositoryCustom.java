package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepositoryCustom {

    Page<UserProfileResponse> searchUsers(
            String keyword,
            Long roleId,
            String status,
            Pageable pageable
    );

    Optional<UserProfileResponse> findProfileById(Long userId);

    Optional<UserProfileResponse> findProfileByCustomerId(Long customerId);
}
