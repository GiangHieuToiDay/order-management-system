package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface RedisService {

    String buildCacheKeyFilterProduct(ProductFilterRequest filterRequest);

    Object get(String key);

    void set(String key, Object value, long timeout, TimeUnit unit);

    void clearCachePattern(String pattern);

    // =========================================================================
    // // [DECOUPLED STOCK CACHING] Các phương thức quản lý tồn kho độc lập trên Redis
    // =========================================================================

    // // [DECOUPLED STOCK CACHING] Lấy số lượng tồn kho realtime của 1 sản phẩm từ Redis key "stock:product:{id}"
    Integer getStock(Long productId);

    void setStock(Long productId, Integer quantity);

    // // [DECOUPLED STOCK CACHING] Lấy tồn kho của nhiều sản phẩm cùng lúc bằng lệnh MGET siêu nhanh (~0.2ms)
    Map<Long, Integer> getMultiStocks(List<Long> productIds);

    // // [DECOUPLED STOCK CACHING] Trừ tồn kho nguyên tử (Atomic DECRBY) khi đặt hàng thành công
    Long deductStock(Long productId, int quantity);

    // // [DECOUPLED STOCK CACHING] Hoàn/cộng tồn kho nguyên tử (Atomic INCRBY) khi hủy đơn hàng hoặc nhập thêm
    Long addStock(Long productId, int quantity);
}
