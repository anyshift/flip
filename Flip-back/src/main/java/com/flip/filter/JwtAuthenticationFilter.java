package com.flip.filter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flip.domain.dto.LoggedUser;
import com.flip.domain.entity.User;
import com.flip.domain.enums.ResponseCode;
import com.flip.service.UserService;
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

    @Value("${jwt.auto-refresh-ttl}")
    private Long autoRefreshTTL;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

        String tokenFromRequestHeader = request.getHeader(tokenHeader);
        if (StrUtil.isBlank(tokenFromRequestHeader) || !tokenFromRequestHeader.startsWith(tokenPrefix)) {
            filterChain.doFilter(request, response); /* 请求头中未携带token或token格式有误的话, 放行 */
            return;
        }

        // 请求头中解析得到的 token 可能的类型有两种：accessToken 和 refreshToken，前者用于日常请求，后者用于刷新前者，实现不中断的操作体验
        String token = tokenFromRequestHeader.replace(tokenPrefix, "");
        String uid;

        boolean isTokenValid;
        try {
            isTokenValid = jwtUtils.isTokenValid(token);
        } catch (Exception e) {
            invalidTokenRender(response);
            return;
        }

        String requestURI = request.getRequestURI();
        if (isTokenValid) {
            Claims claims = jwtUtils.parseTokenToClaims(token);
            String tokenType = claims.getAudience();
            User user = userService.loadUserByUid(Long.parseLong(claims.getSubject()));
            if (ObjectUtil.notEqual("/refresh", requestURI)) { // 非刷新凭证的操作
                switch (tokenType) {
                    case "refresh":
                        uid = null;
                        log.warn("用户 '{}' 携带的凭证与请求类型不匹配", user.getUsername());
                        break;
                    case "access":
                        uid = claims.getSubject();
                        if (ObjectUtil.notEqual("/logout", requestURI) & (claims.getExpiration().getTime() - DateUtil.date().getTime()) <= autoRefreshTTL) {
                            String accessToken = jwtUtils.createAccessToken(uid);
                            response.setHeader(tokenHeader, accessToken);
                            log.info("用户 '{}' 的 accessToken 临近过期且仍然活跃，已重新颁发", user.getUsername());
                        }
                        break;
                    default:
                        uid = null;
                        log.warn("用户 '{}' 携带的凭证的类型有误", user.getUsername());
                        break;
                }
            }  else { // 刷新凭证的操作
                switch (tokenType) {
                    case "access":
                        uid = null;
                        log.warn("用户 '{}' 携带的凭证与请求类型不匹配", user.getUsername());
                        break;
                    case "refresh":
                        uid = claims.getSubject();
                        break;
                    default: {
                        uid = null;
                        log.warn("用户 '{}' 携带的凭证的类型有误", user.getUsername());
                        break;
                    }
                }
            }
        } else {
            if (ObjectUtil.equal("/logout", requestURI)) {
                User user = userService.loadUserByUid(Long.parseLong(jwtUtils.getExpiredTokenClaims(token).getSubject()));
                log.info("用户 '{}' 已退出登录", user.getUsername());
                loggedOutRender(response); // 如果 accessToken 过期，且访问的是 "/logout"，则返回已退出的响应
                return;
            } else if (ObjectUtil.equal("/refresh", requestURI)) {
                loginAgainRender(response); // refreshToken 过期则需要重新登录（JwtUtil中已打印此日志，此处省略过期日志的打印）
                return;
            } else {
                expireTokenRender(response); // accessToken 过期则返回过期信息，前端再通过 refreshToken 尝试获取新的 accessToken
                return;
            }
        }

        LoggedUser loggedUser = (LoggedUser) redisTemplate.opsForValue().get(RedisKeyUtils.getLoggedUserKey(uid));
        if (ObjectUtil.isNull(loggedUser)) {
            User user = userService.loadUserByUid(Long.parseLong(jwtUtils.getExpiredTokenClaims(token).getSubject()));
            log.info("用户 '{}' 的登录缓存丢失，已退出登录", user.getUsername());
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
        render(ResponseCode.UNAUTHORIZED.getCode(), "访问凭证已过期", response);
    }

    private void loginAgainRender(HttpServletResponse response) throws IOException {
        render(ResponseCode.AUTHENTICATION_EXPIRED.getCode(), "请重新登录", response);
    }

    private void invalidTokenRender(HttpServletResponse response) throws IOException {
        render(ResponseCode.PRECONDITION_FAILED.getCode(), "无效凭证", response);
    }

    private void loggedOutRender(HttpServletResponse response) throws IOException {
        render(ResponseCode.SUCCESS.getCode(), "已退出登录", response);
    }

    private void render(int code, String msg, HttpServletResponse response)  throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", msg);

        response.setContentType("application/json;charset=UTF-8");

        String result = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(map);

        try {
            response.getWriter().println(result);
        } finally {
            response.getWriter().close();
        }
    }
}
