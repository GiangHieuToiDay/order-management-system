package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;

import java.util.concurrent.TimeUnit;

public interface RedisService {

    String buildCacheKeyFilterProduct(ProductFilterRequest filterRequest);

    Object get(String key);

    void set(String key, Object value, long timeout, TimeUnit unit);


}
