package com.flip.controller.common;

import com.flip.domain.Response;
import com.flip.entity.User;
import com.flip.service.impl.AccountServiceImpl;
import com.flip.validation.VG;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 账号 登录、注册、验证 控制器
 */
@Slf4j
@Validated(VG.class)
@RestController
public class AccountController {

    private final AccountServiceImpl accountService;

    public AccountController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }

    /**
     * 用户注册
     * @param user 用户实体类
     * @param request 请求对象
     * @param code 图形验证码
     * @param codeOwner 图形验证码所属者
     * @return 响应体
     */
    @PostMapping("/register")
    public Response<Object> register(@RequestBody @Validated(VG.class) User user, HttpServletRequest request,
                                     @RequestParam String code, @RequestParam String codeOwner) {
        return accountService.register(user, request, code, codeOwner);
    }

    @PostMapping("/login")
    public Response<Object> login(@RequestBody User user, @RequestParam String code, @RequestParam String codeOwner) {
        return accountService.login(user, code, codeOwner);
    }

    @GetMapping("/activate")
    public Response<String> getEmailCode(@RequestParam String uid) {
        return accountService.getEmailCode(uid);
    }

    @PutMapping("/activate")
    public Response<Object> activateEmail(@RequestBody User user, @RequestParam String emailCode) {
        return accountService.activateEmail(user, emailCode);
    }

    @PostMapping("/refresh")
    public Response<Object> refreshToken(@NotBlank(message = "refreshToken不能为空", groups = {VG.First.class}) @RequestParam String refreshToken) {
        return accountService.refreshToken(refreshToken);
    }

    /**
     * 校验用户名唯一性
     * @param username 用户名
     * @return 响应体
     */
    @GetMapping("/_checkUsernameUnique")
    public Response<Object> checkUsernameUnique(@RequestParam String username) {
        return accountService.checkUsernameUnique(username);
    }

    /**
     * 获取 Hutool 图片验证码
     * @return 验证码响应体
     */
    @GetMapping("/_captcha")
    public Response<String> getCaptcha() {
        return accountService.getCaptcha();
    }

    /**
     * 校验图形验证码输入是否正确
     * @param code 图形验证码
     * @param codeOwner 图形验证码所属者
     * @return 响应体
     */
    @GetMapping("/_checkCaptcha")
    public Response<Object> checkCaptcha(@RequestParam String code, @RequestParam String codeOwner) {
        return accountService.checkCaptcha(code, codeOwner);
    }
}
