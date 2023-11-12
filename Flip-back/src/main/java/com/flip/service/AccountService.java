package com.flip.service;

import com.flip.common.Response;
import com.flip.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AccountService {

    Boolean sendEmailCode(String to, String emailCode);

    Boolean register(User user, HttpServletRequest request, String captcha, String captchaOwner);

    Boolean checkUsernameUnique(String username);

    Response<Object> checkEmailUnique(String email);
}
