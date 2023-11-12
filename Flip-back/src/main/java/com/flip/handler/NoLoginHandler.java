package com.flip.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flip.domain.enums.ResponseCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 匿名用户(未登录)访问权限资源时的处理
 */
@Component
public class NoLoginHandler implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public NoLoginHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Map<String, Object> map = new HashMap<>();
        map.put("code", ResponseCode.FORBIDDEN.getCode());
        map.put("message", "请登录后再尝试");

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
