package com.trainning.ordersystem.security.jwt;

import com.trainning.ordersystem.security.userDetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Step 1: sinh & verify token
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}") private String secret;      // load từ ENV, KHÔNG hardcode
    @Value("${jwt.expiration}") private long accessTokenExp;   // ví dụ 15 phút
    @Value("${jwt.refresh-expiration}") private long refreshTokenExp;



    // Step 1: Mã hóa key và tạo ra 1 đối tượng Key
    private Key key(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Step 2: Genarate token & Validate Token, token sẽ ở dạng stateless không lưu trạng thái k lưu thông tin
    // Tại sao k truyền vào username hoặc id mà truyền vào Authentication
    // Nếu lúc tạo token chỉ đưa đúng username vào thì khi mình extract token ra thì chỉ có đúng id, muốn lấy các thông tin khác thì phải querry lại
    public String generateToken(Authentication auth){
        // Principal: danh tính người dùng
        // tại sao lại là auth.getPrincipal => bởi vì khi mình login xong mình sẽ lưu các lại cái user login đó vào trong principal của auth.
        CustomUserDetails user =  (CustomUserDetails) auth.getPrincipal();

        String jti = UUID.randomUUID().toString();

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setId(jti)
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExp))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(Authentication auth) {
        return generateToken(auth);
    }

    public String generateRefreshToken(String username) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setId(jti)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExp))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch(JwtException | IllegalArgumentException e){
            log.warn("[JwtTokenProvider] Token không hợp lệ: {}", e.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String getJti(String token)      { return parseClaims(token).getId(); }
    public Date getExpiration(String token) { return parseClaims(token).getExpiration(); }

    public long getAccessTokenExp() {
        return accessTokenExp;
    }

    public long getRefreshTokenExp() {
        return refreshTokenExp;
    }






}
