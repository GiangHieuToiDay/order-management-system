package com.trainning.ordersystem.dto.request.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;

    @NotNull(message = "Số lượng tồn thực tế không được để trống")
    @Min(value = 0, message = "Số lượng thực tế không được âm")
    private Integer actualQuantity;

    @NotBlank(message = "Lý do điều chỉnh không được để trống")
    @Size(max = 255, message = "Lý do tối đa 255 ký tự")
    private String reason;
}
