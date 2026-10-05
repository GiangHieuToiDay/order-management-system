package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.product.ProductCreateRequest;
import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;
import com.trainning.ordersystem.dto.request.product.ProductUpdateRequest;
import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import com.trainning.ordersystem.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo sản phẩm thành công", response));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getProducts(
            @ModelAttribute ProductFilterRequest filterRequest) {
        PageResponse<ProductSummaryResponse> response = productService.getProducts(filterRequest);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách sản phẩm thành công", response));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        ProductDetailResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin sản phẩm thành công", response));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductDetailResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin sản phẩm thành công", response));
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProductStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status) {
        ProductDetailResponse response = productService.updateProductStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái sản phẩm thành công", response));
    }


    @PatchMapping("/{id}/category/{categoryId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> assignCategory(
            @PathVariable Long id,
            @PathVariable Long categoryId) {
        ProductDetailResponse response = productService.assignCategory(id, categoryId);
        return ResponseEntity.ok(ApiResponse.ok("Gán danh mục cho sản phẩm thành công", response));
    }


    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkProductAvailability(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity) {
        boolean available = productService.checkProductAvailability(id, quantity);
        return ResponseEntity.ok(ApiResponse.ok("Kiểm tra tính khả dụng thành công", available));
    }
}
