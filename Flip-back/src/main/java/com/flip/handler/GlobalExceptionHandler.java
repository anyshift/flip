package com.flip.handler;

import com.flip.common.Response;
import com.flip.domain.enums.ResponseCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 全局处理数据校验不一致产生的异常，当 @Validated 修饰 java 对象时引起
     * @param e 错误类型对象
     * @return 响应体
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Response<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, Object> map = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError)error).getField();
            String errorMsg = error.getDefaultMessage();
            map.put(fieldName, errorMsg);
        });
        return Response.failed("参数校验失败", map);
    }

    /**
     * 当 @Validated 修饰单个参数时（此时 @Validated 在类上而不是在方法中）
     * @param e 错误类型对象
     * @return 响应体
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public Response<String> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(","));
        return Response.failed("参数校验失败", message);
    }

    /**
     * 全局处理参数未提供异常，异常由 @RequestParam(required = true) 引起。
     * @param e 错误类型对象
     * @return 响应体
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public Response<String> handleMissingRequestParameterException(MissingServletRequestParameterException e) {
        String parameterName = e.getParameterName();
        String message = "需要提供 " + parameterName + " 参数";
        return Response.failed("参数缺失", message);
    }

    /**
     * authenticationManager.authenticate() 验证登录用户的用户名和密码时，如果匹配不成功，则会抛出AuthenticationException
     * @return 响应体
     */
    @ExceptionHandler({AuthenticationException.class})
    public Response<String> handleAuthenticationException(AuthenticationException e) {
        return Response.failed(ResponseCode.FORBIDDEN.getCode(), "用户名或密码错误");
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    public Response<Object> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return Response.failed(ResponseCode.FORBIDDEN.getCode(), e.getMessage());
    }
}
