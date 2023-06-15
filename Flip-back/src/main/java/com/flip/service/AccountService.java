package com.flip.service;

import com.flip.domain.Response;
import com.flip.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AccountService {
    Response<String> getCaptcha();

    Response<Object> checkCaptcha(String code, String codeOwner);

    Response<String> getEmailCode(String uid);

    Response<String> sendEmailCode(String to, String emailCode);

    Response<Object> register(User user, HttpServletRequest request, String captcha, String captchaOwner);

    Response<Object> checkUsernameUnique(String username);

    Response<Object> checkEmailUnique(String email);

    Response<Object> login(User user, String code, String codeOwner);

    Response<Object> refreshToken(String refreshToken);

    Response<Object> activateEmail(User user, String emailCode);
}
