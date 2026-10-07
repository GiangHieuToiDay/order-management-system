package com.trainning.ordersystem.security.jwt;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "refresh_token:";

    // Lưu refresh token, key theo username + jti để hỗ trợ multi-device
    public void save(String username, String jti, long ttlMillis) {
        String key = PREFIX + username + ":" + jti;
        redisTemplate.opsForValue().set(key, "valid", Duration.ofMillis(ttlMillis));
    }

    public boolean isValid(String username, String jti) {
        String key = PREFIX + username + ":" + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Revoke 1 token cụ thể (dùng khi logout hoặc rotation)
    public void revoke(String username, String jti) {
        redisTemplate.delete(PREFIX + username + ":" + jti);
    }


    // Revoke toàn bộ refresh token của user (dùng khi phát hiện bị compromise, hoặc "logout all devices")
    public void revokeAll(String username) {
        Set<String> keys = redisTemplate.keys(PREFIX + username + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

}
