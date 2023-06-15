package com.flip.controller.common;

import com.flip.domain.Response;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/testRole")
    public Response<Object> testRole() {
        return Response.success(200, "测试角色成功");
    }

    @GetMapping("/testPermission")
    public Response<Object> testPermission() {
        return Response.success(200, "测试权限成功");
    }
}
