package com.iven.memo.utils;

import com.iven.memo.exceptions.JwtForbidden;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    // 使用固定的密钥，避免每次重启都生成新密钥
    private long EXPIRATION_TIME;
    private Key key;

    @Value("${security.jwt.secret}")
    private String secretKey;
    @Value("${security.jwt.expire-day}")
    private int expireDay;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.EXPIRATION_TIME = expireDay * 24L * 60L * 60L * 1000L;
    }

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }
    public String generateToken(Long userId) {
        return generateToken(String.valueOf(userId));
    }

    public String validateToken(String token) throws ExpiredJwtException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (UnsupportedJwtException e) {
            throw new JwtForbidden("不支持的JWT token格式");
        } catch (MalformedJwtException e) {
            throw new JwtForbidden("JWT token格式错误");
        } catch (SecurityException e) {
            throw new JwtForbidden("JWT签名验证失败");
        } catch (IllegalArgumentException e) {
            throw new JwtForbidden("JWT token为空或格式错误");
        }
    }
}