package com.trainning.ordersystem.security.jwt;

import com.trainning.ordersystem.security.userDetails.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Step 2: filter đọc token mỗi request
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService blacklistService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService, TokenBlacklistService blacklistService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            if (!tokenProvider.validateToken(token)) {
                log.warn("[JwtFilter] Token không hợp lệ hoặc đã hết hạn cho request: {} {}", request.getMethod(), request.getRequestURI());
            } else {
                String jti = tokenProvider.getJti(token);

                if (blacklistService.isBlacklisted(jti)) {
                    log.warn("[JwtFilter] Token đã bị thu hồi trong Redis (jti={})", jti);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"code\":1004,\"message\":\"Token đã bị thu hồi. Vui lòng đăng nhập lại!\"}");
                    return;
                }

                try {
                    String username = tokenProvider.getUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (userDetails != null && userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("[JwtFilter] Xác thực thành công cho user: {}, quyền: {}", username, userDetails.getAuthorities());
                    } else {
                        log.warn("[JwtFilter] User '{}' không tồn tại, bị disable hoặc locked", username);
                    }
                } catch (Exception e) {
                    log.error("[JwtFilter] Lỗi khi load UserDetails cho user từ token: {}", e.getMessage(), e);
                    SecurityContextHolder.clearContext();
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
