package com.flip.filter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flip.entity.dto.LoggedUser;
import com.flip.utils.JwtUtils;
import com.flip.utils.RedisKeyUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Token校验过滤器，在每次前端请求前，检查是否携带Token。
 * 没有携带就放行去执行后续方法，携带了就解析出当前访问者的所有信息，然后存入SecurityContext。
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.token-header}")
    private String tokenHeader;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

        String tokenFromRequestHeader = request.getHeader(tokenHeader);
        if (StrUtil.isBlank(tokenFromRequestHeader) || !tokenFromRequestHeader.startsWith(tokenPrefix)) {
            /* 请求头中未携带token或token格式有误的话, 放行 */
            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenFromRequestHeader.replace(tokenPrefix, "");
        String uid;

        boolean isTokenValid;
        try {
            isTokenValid = jwtUtils.isTokenValid(token);
        } catch (Exception e) {
            invalidTokenRender(response);
            return;
        }

        if (isTokenValid) {
            Claims claims = jwtUtils.parseTokenToClaims(token);
            String tokenType = claims.getAudience();
            if (ObjectUtil.notEqual("/refresh", request.getRequestURI())) { // 非刷新token的操作
                switch (tokenType) {
                    case "refresh" -> {
                        uid = null;
                        log.warn("The token with invalid type is carried by {}, need access type, but get refresh type.", claims.getSubject());
                    }
                    case "access" -> {
                        uid = claims.getSubject();
                        if (ObjectUtil.notEqual("/logout", request.getRequestURI()) & (claims.getExpiration().getTime() - DateUtil.date().getTime()) <= 20 * 60 * 1000L) {
                            String accessToken = jwtUtils.createAccessToken(uid);
                            response.setHeader(tokenHeader, accessToken);
                            log.info("{}'s token refreshed.", claims.getSubject());
                        }
                    }
                    default -> {
                        uid = null;
                        log.warn("{} carried an unknown type of token during non-refresh operation.", claims.getSubject());
                    }
                }
            } else { //刷新token的操作
                switch (tokenType) {
                    case "access" -> {
                        uid = null;
                        log.warn("The refreshToken with invalid type is carried by {}, need refresh type, but get access type.", claims.getSubject());
                    }
                    case "refresh" -> uid = claims.getSubject();
                    default -> {
                        uid = null;
                        log.warn("{} carries an unknown type of token during refresh operation.", claims.getSubject());
                    }
                }
            }
        } else {
            if (ObjectUtil.equal("/logout", request.getRequestURI())) {
                log.info("{} token expired then auto logout (/logout).", jwtUtils.getExpiredTokenClaims(token).getSubject());
                loggedOutRender(response); // 如果token过期，且访问的是/logout，则显示退出成功
                return;
            } else if (ObjectUtil.equal("/refresh", request.getRequestURI())) {
                loginAgainRender(response); // refreshToken过期则需要重新登录
                return;
            } else {
                expireTokenRender(response); // accessToken过期则返回过期信息，前端再通过refreshToken尝试获取新的accessToken
                return;
            }
        }

        LoggedUser loggedUser = (LoggedUser) redisTemplate.opsForValue().get(RedisKeyUtils.getLoggedUserKey(uid));
        if (ObjectUtil.isNull(loggedUser)) {
            log.info("{} needs to log in again, so auto logout", jwtUtils.getExpiredTokenClaims(token).getSubject());
            loginAgainRender(response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loggedUser, null, loggedUser.getAuthorities());

        /* convert an instance of HttpServletRequest class into an instance of the WebAuthenticationDetails class */
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        /* 把当前访问者的信息存入SecurityContext，后续就能通过 SecurityContextHolder.getContext() 获取到该登录者的详细信息 */
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private void expireTokenRender(HttpServletResponse response) throws IOException {
        render(401, "登录凭证已过期", response);
    }

    private void loginAgainRender(HttpServletResponse response) throws IOException {
        render(409, "请重新登录", response);
    }

    private void invalidTokenRender(HttpServletResponse response) throws IOException {
        render(412, "Invalid Token", response);
    }

    private void loggedOutRender(HttpServletResponse response) throws IOException {
        render(200, "已退出登录", response);
    }

    private void render(int code, String msg, HttpServletResponse response)  throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);

        response.setContentType("application/json;charset=UTF-8");

        String result = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(map);

        response.getWriter().println(result);
    }
}
