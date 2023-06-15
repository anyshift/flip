package com.flip.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 统一响应结果
 * @param <T>
 */

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    private Integer code;
    private String msg;
    private Map<String, T> data;

    public Response(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(Integer code, Map<String, T> data) {
        this.code = code;
        this.data = data;
    }

    public static <T> Response<T> success(Integer code, String msg, Map<String, T> data) {
        return new Response<>(code, msg, data);
    }

    public static <T> Response<T> success(Integer code, Map<String, T> data) {
        return new Response<>(code, data);
    }

    public static <T> Response<T> success(Integer code, String msg) {
        return new Response<>(code, msg);
    }

    public static <T> Response<T> error(Integer code, String msg) {
        return new Response<>(code, msg);
    }

    public static <T> Response<T> error(Integer code, Map<String, T> data) {
        return new Response<>(code, data);
    }

    public static <T> Response<T> error(Integer code, String msg, Map<String, T> data) {
        return new Response<>(code, msg, data);
    }
}
