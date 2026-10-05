package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.product.ProductCreateRequest;
import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;
import com.trainning.ordersystem.dto.request.product.ProductUpdateRequest;
import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.Category;
import com.trainning.ordersystem.entity.Product;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import com.trainning.ordersystem.mapper.ProductMapper;
import com.trainning.ordersystem.repository.ProductRepository;
import com.trainning.ordersystem.service.ProductService;
import com.trainning.ordersystem.specification.CategorySpecification;
import com.trainning.ordersystem.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSpecification productSpecification;
    private final CategorySpecification categorySpecification;
    private final ProductMapper productMapper;


    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        log.info("Tạo sản phẩm mới: sku={}, name={}", request.getSku(), request.getName());

        productSpecification.validateSkuNotExists(request.getSku());
        Category category = categorySpecification.getCategoryById(request.getCategoryId());

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDetailResponse(savedProduct);
    }


    @Override
    public PageResponse<ProductSummaryResponse> getProducts(ProductFilterRequest request) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String sortBy = (request.getSortBy() != null && !request.getSortBy().isBlank())
                ? request.getSortBy()
                : "id";

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortBy));

        Page<Product> productPage = productRepository.searchProducts(
                request.getKeyword(),
                request.getCategoryId(),
                request.getStatus(),
                request.getMinPrice(),
                request.getMaxPrice(),
                pageable
        );

        return PageResponse.<ProductSummaryResponse>builder()
                .content(productMapper.toSummaryResponseList(productPage.getContent()))
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .isFirst(productPage.isFirst())
                .isLast(productPage.isLast())
                .build();
    }


    @Override
    public ProductDetailResponse getProductById(Long id) {
        Product product = productSpecification.getProductById(id);
        return productMapper.toDetailResponse(product);
    }


    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.info("Cập nhật thông tin sản phẩm: id={}", id);

        Product product = productSpecification.getProductById(id);
        productSpecification.validateSkuNotExistsForUpdate(id, request.getSku());

        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categorySpecification.getCategoryById(request.getCategoryId());
            product.setCategory(category);
        }

        productMapper.updateEntityFromRequest(request, product);
        Product updatedProduct = productRepository.save(product);

        return productMapper.toDetailResponse(updatedProduct);
    }


    @Override
    @Transactional
    public ProductDetailResponse updateProductStatus(Long id, ProductStatus status) {
        log.info("Cập nhật trạng thái sản phẩm: id={}, status={}", id, status);

        productSpecification.validateStatus(status);
        Product product = productSpecification.getProductById(id);
        product.setStatus(status);
        Product savedProduct = productRepository.save(product);

        return productMapper.toDetailResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductDetailResponse assignCategory(Long productId, Long categoryId) {
        log.info("Gán danh mục cho sản phẩm: productId={}, categoryId={}", productId, categoryId);

        Product product = productSpecification.getProductById(productId);
        Category category = categorySpecification.getCategoryById(categoryId);

        product.setCategory(category);
        Product savedProduct = productRepository.save(product);

        return productMapper.toDetailResponse(savedProduct);
    }


    @Override
    public boolean checkProductAvailability(Long productId, int quantity) {
        Product product = productSpecification.getProductById(productId);
        return productSpecification.isAvailable(product, quantity);
    }
}
