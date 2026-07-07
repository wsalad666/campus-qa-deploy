package com.example.newtrial2.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class AdminJwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(AdminJwtUtil.class);

    @Value("${admin.jwt.secret}")
    private String secretKey;

    @Value("${admin.jwt.expiration}")
    private long expirationTime;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long adminId) {
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(Long adminId, Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Long getAdminIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            logger.error("解析管理员Token失败", e);
            return null;
        }
    }

    /**
     * 从管理员Token中获取关联的userId
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            return null;
        } catch (Exception e) {
            logger.error("解析管理员Token中的userId失败", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 判断是否需要续期token
     * 当token剩余有效期不足总有效期的50%时，返回true
     */
    public boolean shouldRenewToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remainingTime = expiration.getTime() - now.getTime();
            // 如果剩余时间不足总有效期的50%，需要续期
            return remainingTime < (expirationTime / 2);
        } catch (Exception e) {
            logger.error("判断token续期失败", e);
            return false;
        }
    }
}