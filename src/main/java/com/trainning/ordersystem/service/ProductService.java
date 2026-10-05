package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.product.ProductCreateRequest;
import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;
import com.trainning.ordersystem.dto.request.product.ProductUpdateRequest;
import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.enums.ProductStatus;

public interface ProductService {


    ProductDetailResponse createProduct(ProductCreateRequest request);

    PageResponse<ProductSummaryResponse> getProducts(ProductFilterRequest request);

    ProductDetailResponse getProductById(Long id);

    ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request);

    ProductDetailResponse updateProductStatus(Long id, ProductStatus status);

    ProductDetailResponse assignCategory(Long productId, Long categoryId);

    boolean checkProductAvailability(Long productId, int quantity);
}
