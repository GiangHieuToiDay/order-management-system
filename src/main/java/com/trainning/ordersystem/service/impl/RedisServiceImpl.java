package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;
import com.trainning.ordersystem.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public String buildCacheKeyFilterProduct(ProductFilterRequest request) {
        return String.format(
                "product:list:%s|%s|%s|%s|%s|%s|%s|%s|%s",
                request.getKeyword(),
                request.getCategoryId(),
                request.getStatus(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getPage(),
                request.getSize(),
                request.getSortBy(),
                request.getSortDirection()
        );
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

}
