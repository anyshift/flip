package com.flip.handler.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限不足时的处理
 */
@Component
public class NoPermissionHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public NoPermissionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 403);
        map.put("msg", "权限不足哦～");
        response.setContentType("application/json;charset=UTF-8");
        String result = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(map);

        response.getWriter().println(result);
    }
}
