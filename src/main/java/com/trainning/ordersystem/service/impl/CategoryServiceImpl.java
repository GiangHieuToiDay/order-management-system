package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.category.CategoryRequest;
import com.trainning.ordersystem.dto.response.category.CategoryResponse;
import com.trainning.ordersystem.entity.Category;
import com.trainning.ordersystem.entity.enums.CategoryStatus;
import com.trainning.ordersystem.mapper.CategoryMapper;
import com.trainning.ordersystem.repository.CategoryRepository;
import com.trainning.ordersystem.service.CategoryService;
import com.trainning.ordersystem.specification.CategorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategorySpecification categorySpecification;
    private final CategoryMapper categoryMapper;



    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Tạo danh mục mới: name={}", request.getName());

        categorySpecification.validateNameNotExists(request.getName());

        Category category = categoryMapper.toEntity(request);
        if (category.getStatus() == null) {
            category.setStatus(CategoryStatus.ACTIVE);
        }
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }


    @Override
    public PageResponse<CategoryResponse> getCategories(int page, int size, String keyword, CategoryStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Category> categoryPage = categoryRepository.searchCategories(keyword, status, pageable);

        return PageResponse.<CategoryResponse>builder()
                .content(categoryMapper.toResponseList(categoryPage.getContent()))
                .pageNumber(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .isFirst(categoryPage.isFirst())
                .isLast(categoryPage.isLast())
                .build();
    }

    @Override
    public List<CategoryResponse> getActiveCategories() {
        List<Category> activeCategories = categoryRepository.findByStatus(CategoryStatus.ACTIVE);
        return categoryMapper.toResponseList(activeCategories);
    }


    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categorySpecification.getCategoryById(id);
        return categoryMapper.toResponse(category);
    }


    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Cập nhật danh mục: id={}, name={}", id, request.getName());

        Category category = categorySpecification.getCategoryById(id);
        categorySpecification.validateNameNotExistsForUpdate(id, request.getName());

        categoryMapper.updateEntityFromRequest(request, category);
        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }


    @Override
    @Transactional
    public CategoryResponse updateCategoryStatus(Long id, CategoryStatus status) {
        log.info("Cập nhật trạng thái danh mục: id={}, status={}", id, status);

        Category category = categorySpecification.getCategoryById(id);
        category.setStatus(status);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Yêu cầu xóa danh mục: id={}", id);

        Category category = categorySpecification.getCategoryById(id);
        categorySpecification.validateCanDelete(id);

        categoryRepository.delete(category);
        log.info("Đã xóa danh mục id={}", id);
    }
}
