package com.flip.domain.enums;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS(200, "请求执行成功"),

    /**
     * 遇到错误，用户需求无法完成
     */
    BAD_REQUEST(400, "请求执行出现错误"),

    /**
     * accessToken 过期
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 用户权限不足
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求携带错误类型的 token，如需要 accessToken 但解析得到的是 refreshToken
     */
    PRECONDITION_FAILED(412, "前提条件不匹配"),

    /**
     * accessToken 与 refreshToken 双双过期
     */
    AUTHENTICATION_EXPIRED(419, "身份过期"),

    /**
     * 内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
