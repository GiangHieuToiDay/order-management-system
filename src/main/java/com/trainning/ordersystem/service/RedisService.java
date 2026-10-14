package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface RedisService {

    String buildCacheKeyFilterProduct(ProductFilterRequest filterRequest);

    Object get(String key);

    void set(String key, Object value, long timeout, TimeUnit unit);

    void clearCachePattern(String pattern);


    Integer getStock(Long productId);

    void setStock(Long productId, Integer quantity);

    Map<Long, Integer> getMultiStocks(List<Long> productIds);

    Long deductStock(Long productId, int quantity);

    Long addStock(Long productId, int quantity);

     <T> T executeWithProductLocks(List<Long> productIds, Supplier<T> action);

}
