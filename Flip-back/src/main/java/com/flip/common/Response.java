package com.flip.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flip.domain.enums.ResponseCode;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    public Response(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Response(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public Response(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(Integer code, String message, T data) {
        return new Response<>(code, message, data);
    }

    public static <T> Response<T> success(String message, T data) {
        return new Response<>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Response<T> success(Integer code, String message) {
        return new Response<>(code, message);
    }

    public static <T> Response<T> success(String message) {
        return new Response<>(ResponseCode.SUCCESS.getCode(), message);
    }

    public static <T> Response<T> failed(Integer code, String message, T data) {
        return new Response<>(code, message, data);
    }

    public static <T> Response<T> failed(String message, T data) {
        return new Response<>(ResponseCode.BAD_REQUEST.getCode(), message, data);
    }

    public static <T> Response<T> failed(Integer code, String message) {
        return new Response<>(code, message);
    }

    public static <T> Response<T> failed(String message) {
        return new Response<>(ResponseCode.BAD_REQUEST.getCode(), message);
    }
}
