package com.trainning.ordersystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===================================================================
    // 1. HỆ THỐNG & CHUNG (9xxx)
    // ===================================================================
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(9001, "Dữ liệu yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),

    // ===================================================================
    // 2. NGƯỜI DÙNG & XÁC THỰC (1xxx)
    // ===================================================================
    USER_NOT_FOUND(1001, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(1002, "Tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1003, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1004, "Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    CUSTOMER_NOT_FOUND(1005, "Thông tin khách hàng không tồn tại", HttpStatus.NOT_FOUND),
    ACCESS_DENIED(1006, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(1007, "Vai trò không tồn tại", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS(1008, "Vai trò đã tồn tại", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1009, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!", HttpStatus.FORBIDDEN),
    CANNOT_LOCK_CURRENT_USER(1010, "Bạn không thể tự khóa tài khoản của chính mình", HttpStatus.BAD_REQUEST),

    // ===================================================================
    // 3. DANH MỤC & SẢN PHẨM (2xxx)
    // ===================================================================
    CATEGORY_NOT_FOUND(2001, "Danh mục không tồn tại", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_DUPLICATE(2002, "Tên danh mục đã tồn tại", HttpStatus.BAD_REQUEST),
    CATEGORY_HAS_PRODUCTS(2003, "Danh mục đang chứa sản phẩm, không thể xóa", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(2004, "Sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    SKU_ALREADY_EXISTS(2005, "Mã SKU sản phẩm đã tồn tại", HttpStatus.BAD_REQUEST),

    // ===================================================================
    // 4. KHO HÀNG (3xxx)
    // ===================================================================
    INSUFFICIENT_STOCK(3001, "Số lượng tồn kho không đủ đáp ứng", HttpStatus.BAD_REQUEST),

    // ===================================================================
    // 5. GIỎ HÀNG (4xxx)
    // ===================================================================
    CART_ITEM_NOT_FOUND(4001, "Sản phẩm không có trong giỏ hàng", HttpStatus.NOT_FOUND),

    // ===================================================================
    // 6. ĐƠN HÀNG (5xxx)
    // ===================================================================
    ORDER_NOT_FOUND(5001, "Đơn hàng không tồn tại", HttpStatus.NOT_FOUND),
    EMPTY_ORDER_ITEMS(5002, "Đơn hàng phải có ít nhất 1 sản phẩm", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS(5003, "Không thể chuyển sang trạng thái đơn hàng này", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_ORDER(5004, "Đơn hàng đã hoàn tất hoặc đang giao, không thể hủy", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
