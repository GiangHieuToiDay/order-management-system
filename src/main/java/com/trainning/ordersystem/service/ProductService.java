package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.product.ProductCreateRequest;
import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;
import com.trainning.ordersystem.dto.request.product.ProductUpdateRequest;
import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.enums.ProductStatus;

public interface ProductService {

    /**
     * 1. Tạo sản phẩm mới (SKU duy nhất, giá > 0, danh mục hợp lệ, mặc định ACTIVE)
     * Actor: Admin / Staff
     */
    ProductDetailResponse createProduct(ProductCreateRequest request);

    /**
     * 2. Xem danh sách sản phẩm (Tìm kiếm keyword, lọc category, status, khoảng giá, sắp xếp & phân trang)
     * Actor: Guest / Customer / Staff / Admin
     */
    PageResponse<ProductSummaryResponse> getProducts(ProductFilterRequest request);

    /**
     * 3. Xem chi tiết sản phẩm
     * Actor: Guest / Customer / Staff / Admin
     */
    ProductDetailResponse getProductById(Long id);

    /**
     * 4. Cập nhật thông tin sản phẩm (kiểm tra SKU không trùng sản phẩm khác)
     * Actor: Admin / Staff
     */
    ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request);

    /**
     * 5. Thay đổi trạng thái sản phẩm (ACTIVE ↔ INACTIVE, không xóa vật lý)
     * Actor: Admin
     */
    ProductDetailResponse updateProductStatus(Long id, ProductStatus status);

    /**
     * 6. Gán sản phẩm vào danh mục (chọn category và gán)
     * Actor: Admin / Staff
     */
    ProductDetailResponse assignCategory(Long productId, Long categoryId);

    /**
     * 7. Kiểm tra tính khả dụng của sản phẩm (sản phẩm đang ACTIVE và tồn kho >= số lượng yêu cầu)
     * Actor: Guest / Customer / Staff / Admin / Internal Services
     */
    boolean checkProductAvailability(Long productId, int quantity);
}
