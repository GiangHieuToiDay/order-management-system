package com.trainning.ordersystem.dto.response.category;

import com.trainning.ordersystem.entity.enums.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private CategoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor dùng cho SqlResultSetMapping với String status
    public CategoryResponse(Long id, String name, String description, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, name, description, status != null ? CategoryStatus.valueOf(status) : null, createdAt, updatedAt);
    }
}
