package com.flip.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flip.domain.enums.ResponseCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class NoPermissionHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Map<String, Object> map = new HashMap<>();
        map.put("code", ResponseCode.FORBIDDEN.getCode());
        map.put("message", "权限不足");

        response.setContentType("application/json;charset=UTF-8");
        String result = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(map);

        try {
            response.getWriter().println(result);
        } finally {
            response.getWriter().close();
        }
    }
}
