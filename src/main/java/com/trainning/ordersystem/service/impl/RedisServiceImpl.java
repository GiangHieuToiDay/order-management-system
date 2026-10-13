package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.request.product.ProductFilterRequest;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    private static final String LOCK_KEY_PREFIX = "lock:product:";

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

    @Override
    public void clearCachePattern(String pattern) {
        try {
            java.util.Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // ignore cache clear error
        }
    }

    private static final String STOCK_KEY_PREFIX = "stock:product:";

    @Override
    public Integer getStock(Long productId) {
        if (productId == null) return null;
        try {
            String val = stringRedisTemplate.opsForValue().get(STOCK_KEY_PREFIX + productId);
            if (val == null) return null;
            return Integer.parseInt(val.replace("\"", "").trim());
        } catch (Exception e) {
            log.warn("Lỗi khi đọc tồn kho từ Redis cho productId={}: {}", productId, e.getMessage());
            return null;
        }
    }

    @Override
    public void setStock(Long productId, Integer quantity) {
        if (productId == null || quantity == null) return;
        try {
            stringRedisTemplate.opsForValue().set(STOCK_KEY_PREFIX + productId, quantity.toString(), 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Lỗi khi set tồn kho trên Redis cho productId={}: {}", productId, e.getMessage());
        }
    }

    @Override
    public Map<Long, Integer> getMultiStocks(List<Long> productIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return result;

        try {
            List<String> keys = productIds.stream()
                    .map(id -> STOCK_KEY_PREFIX + id)
                    .toList();
            List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
            if (values != null) {
                for (int i = 0; i < productIds.size(); i++) {
                    Long pId = productIds.get(i);
                    String val = values.get(i);
                    if (val != null) {
                        try {
                            result.put(pId, Integer.parseInt(val.replace("\"", "").trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi multiGet tồn kho từ Redis: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public Long deductStock(Long productId, int quantity) {
        if (productId == null || quantity <= 0) return null;
        try {
            return stringRedisTemplate.opsForValue().decrement(STOCK_KEY_PREFIX + productId, quantity);
        } catch (Exception e) {
            log.error("Lỗi khi trừ tồn kho trên Redis cho productId={}: {}", productId, e.getMessage());
            return null;
        }
    }

    @Override
    public Long addStock(Long productId, int quantity) {
        if (productId == null || quantity <= 0) return null;
        try {
            return stringRedisTemplate.opsForValue().increment(STOCK_KEY_PREFIX + productId, quantity);
        } catch (Exception e) {
            log.error("Lỗi khi cộng tồn kho trên Redis cho productId={}: {}", productId, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T executeWithProductLocks(List<Long> productIds, Supplier<T> action) {
        if (productIds == null || productIds.isEmpty()) {
            return action.get();
        }

        List<Long> sortedIds = new ArrayList<>(new HashSet<>(productIds));
        Collections.sort(sortedIds);

        RLock[] locks = new RLock[sortedIds.size()];
        for (int i = 0; i < sortedIds.size(); i++) {
            locks[i] = redissonClient.getLock(LOCK_KEY_PREFIX + sortedIds.get(i));
        }

        RLock multiLock = redissonClient.getMultiLock(locks);
        boolean isLocked = false;

        try {
            isLocked = multiLock.tryLock(3,  TimeUnit.SECONDS);

            if (!isLocked) {
                throw new AppException(
                        ErrorCode.UNCATEGORIZED_EXCEPTION,
                        "Hệ thống đang bận do có nhiều người cùng mua các sản phẩm này, vui lòng thử lại sau giây lát!"
                );
            }

            return action.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Quá trình xử lý đặt hàng bị gián đoạn");
        } finally {
            if (isLocked) {
                try {
                    multiLock.unlock();
                } catch (IllegalMonitorStateException e) {
                    log.warn("Lock đã mất hiệu lực trước khi unlock", e);
                }
            }
        }
    }


}
