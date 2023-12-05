package com.flip.handler;

import com.flip.domain.dto.LoggedUser;
import com.flip.utils.LoggedUserUtils;
import com.flip.utils.RedisKeyUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutHandler extends SecurityContextLogoutHandler {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoggedUser loggedUser = LoggedUserUtils.getLoggedUser();
        String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(loggedUser.getUser().getUid()));

        redisTemplate.delete(loggedUserKey);
        SecurityContextHolder.clearContext();
        log.info("用户 '{}' 已退出登录", loggedUser.getUser().getUsername());
    }
}
