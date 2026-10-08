package com.trainning.ordersystem.dto.request.order;

import com.trainning.ordersystem.entity.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrderRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    private String recipientName;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải từ 10-11 chữ số")
    private String recipientPhone;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ nhận hàng tối đa 255 ký tự")
    private String shippingAddress;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String note;

    /**
     * true: Đặt hàng từ giỏ hàng hiện tại của customer
     * false: Mua ngay theo danh sách items truyền vào
     */
    @Builder.Default
    private boolean fromCart = false;

    /**
     * Danh sách ID các CartItem được tick chọn mua (nếu fromCart = true).
     * Nếu null hoặc rỗng, mặc định mua toàn bộ giỏ hàng.
     */
    private List<Long> cartItemIds;

    @Valid
    private List<OrderItemRequest> items;
}
