package com.flip.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.flip.domain.entity.User;
import com.flip.service.UserService;
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

    @Resource
    private UserService userService;

    /**
     * accessToken是对已认证用户颁发的凭证，后续请求只要携带上accessToken并解析通过，则对请求放行操作
     * @param uid 用户ID
     * @return accessToken（访问凭证）
     */
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

    /**
     * 由于accessToken过期时间不宜太长，所以会遇到正在进行业务操作时凭证过期的情况。
     * 因此需要一个refreshToken对访问凭证进行无感知刷新的操作，防止降低用户体验。
     * @param uid 用户ID
     * @return refreshToken（刷新访问凭证的凭证）
     */
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
            User user = userService.loadUserByUid(Long.parseLong(e.getClaims().getSubject()));
            switch (e.getClaims().getAudience()) {
                case "access":
                    log.info("用户 '{}' 的 accessToken 已过期", user.getUsername());
                    break;
                case "refresh": {
                    log.info("用户 '{}' 的 refreshToken 已过期，已退出登录", user.getUsername());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("解析凭证出错: {}", e.getMessage());
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
            log.error("错误的凭证: {}", e.getMessage());
            throw new JwtException(e.getMessage());
        }
    }
}
