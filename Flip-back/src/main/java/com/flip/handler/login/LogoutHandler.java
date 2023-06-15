package com.flip.handler.login;

import com.flip.entity.dto.LoggedUser;
import com.flip.utils.RedisKeyUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogoutHandler extends SecurityContextLogoutHandler {

    private final RedisTemplate<String, Object> redisTemplate;

    public LogoutHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedUserKey = RedisKeyUtils.getLoggedUserKey(String.valueOf(loggedUser.getUser().getUid()));

        redisTemplate.delete(loggedUserKey);
        SecurityContextHolder.clearContext();
        log.info("{} logged out", loggedUser.getUser().getUid());
    }
}
