package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.category.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryCustom {

    Page<CategoryResponse> searchCategoryResponses(String keyword, String status, Pageable pageable);

    List<CategoryResponse> findCategoryResponsesByStatus(String status);

    Optional<CategoryResponse> findCategoryResponseById(Long id);
}
