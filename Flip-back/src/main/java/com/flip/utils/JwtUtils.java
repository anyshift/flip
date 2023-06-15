package com.flip.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Getter
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String SIGNATURE;

    @Value("${jwt.accessTokenTTL}")
    private long accessTokenTTL;

    @Value("${jwt.refreshTokenTTL}")
    private long refreshTokenTTl;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public String createAccessToken(String uid) {
        SecretKey sign = Keys.hmacShaKeyFor(SIGNATURE.getBytes(StandardCharsets.UTF_8));
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId(IdUtil.simpleUUID())
                .setSubject(uid)
                .setAudience("access") //标记这是个accessToken
                .signWith(sign, SignatureAlgorithm.HS256)
                .setExpiration(DateUtil.date(System.currentTimeMillis() + accessTokenTTL))
                .compressWith(CompressionCodecs.GZIP);

        return jwtBuilder.compact();
    }

    public String createRefreshToken(String uid) {
        SecretKey sign = Keys.hmacShaKeyFor(SIGNATURE.getBytes(StandardCharsets.UTF_8));
        JwtBuilder jwtBuilder = Jwts.builder()
                .setId(IdUtil.simpleUUID())
                .setSubject(uid)
                .setAudience("refresh") //标记这是个refreshToken
                .signWith(sign, SignatureAlgorithm.HS256)
                .setExpiration(DateUtil.date(System.currentTimeMillis() + refreshTokenTTl))
                .compressWith(CompressionCodecs.GZIP);

        return jwtBuilder.compact();
    }

    public Claims parseTokenToClaims(String token) {
        SecretKey sign = Keys.hmacShaKeyFor(SIGNATURE.getBytes(StandardCharsets.UTF_8));
        if (isTokenValid(token)) {
            return Jwts.parserBuilder().setSigningKey(sign).build().parseClaimsJws(token).getBody();
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        SecretKey sign = Keys.hmacShaKeyFor(SIGNATURE.getBytes(StandardCharsets.UTF_8));

        try {
            Jwts.parserBuilder().setSigningKey(sign).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            switch (e.getClaims().getAudience()) {
                case "access" -> log.info("{}'s token expired.", e.getClaims().getSubject());
                case "refresh" -> {
                    log.info("{}'s refreshToken expired.", e.getClaims().getSubject());
                    log.info("{} auto logout (token all expired).", e.getClaims().getSubject());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("token error: {}", e.getMessage());
            throw new JwtException(e.getMessage());
        }
    }

    public Claims getExpiredTokenClaims(String token) {
        SecretKey sign = Keys.hmacShaKeyFor(SIGNATURE.getBytes(StandardCharsets.UTF_8));
        try {
            return Jwts.parserBuilder().setSigningKey(sign).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            log.error("token error: {}", e.getMessage());
            throw new JwtException(e.getMessage());
        }
    }
}
