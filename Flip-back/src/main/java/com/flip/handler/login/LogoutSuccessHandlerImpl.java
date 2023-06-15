package com.flip.handler.login;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (ObjectUtil.isNull(authentication)) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("msg", "已退出登录");

            response.setContentType("application/json;charset=UTF-8");

            String result = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(map);

            response.getWriter().println(result);
        }
    }
}
