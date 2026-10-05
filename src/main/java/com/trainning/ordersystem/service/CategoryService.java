package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.category.CategoryRequest;
import com.trainning.ordersystem.dto.response.category.CategoryResponse;
import com.trainning.ordersystem.entity.enums.CategoryStatus;

import java.util.List;

public interface CategoryService {

    /**
     * 1. Tạo Category mới (mặc định ACTIVE, kiểm tra không trùng tên)
     * Actor: Admin / Staff
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * 2. Xem danh sách Category (Search theo tên, filter theo status, phân trang)
     * Actor: Guest / Customer / Staff / Admin
     */
    PageResponse<CategoryResponse> getCategories(int page, int size, String keyword, CategoryStatus status);

    /**
     * Lấy danh sách các Category đang ACTIVE (dành cho Guest / Customer xem menu trên storefront)
     */
    List<CategoryResponse> getActiveCategories();

    /**
     * 3. Xem chi tiết Category
     * Actor: Guest / Customer / Staff / Admin
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 4. Cập nhật Category (kiểm tra không trùng tên với category khác)
     * Actor: Admin / Staff
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * 5. Thay đổi trạng thái Category (ACTIVE / INACTIVE)
     * Actor: Admin
     */
    CategoryResponse updateCategoryStatus(Long id, CategoryStatus status);

    /**
     * Xóa Category (Chỉ được xóa khi không còn sản phẩm nào thuộc category đó)
     * Actor: Admin
     */
    void deleteCategory(Long id);
}
