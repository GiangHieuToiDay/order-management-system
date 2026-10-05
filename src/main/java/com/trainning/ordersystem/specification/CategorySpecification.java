package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.entity.Category;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.CategoryRepository;
import com.trainning.ordersystem.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CategorySpecification {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public void validateNameNotExists(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new AppException(
                    ErrorCode.CATEGORY_NAME_DUPLICATE,
                    "Tên danh mục '" + name + "' đã tồn tại"
            );
        }
    }

    public void validateNameNotExistsForUpdate(Long categoryId, String newName) {
        categoryRepository.findByName(newName).ifPresent(existingCategory -> {
            if (!existingCategory.getId().equals(categoryId)) {
                throw new AppException(
                        ErrorCode.CATEGORY_NAME_DUPLICATE,
                        "Tên danh mục '" + newName + "' đã được sử dụng bởi danh mục khác"
                );
            }
        });
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.CATEGORY_NOT_FOUND,
                        "Không tìm thấy danh mục với id = " + categoryId
                ));
    }

    public void validateCanDelete(Long categoryId) {
        if (productRepository.existsByCategoryId(categoryId)) {
            throw new AppException(
                    ErrorCode.CATEGORY_HAS_PRODUCTS,
                    "Danh mục đang chứa sản phẩm, không thể xóa! Vui lòng chuyển trạng thái sang INACTIVE."
            );
        }
    }
}
