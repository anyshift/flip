package com.flip.annotation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * 限制接口调用频率的注解（通用）
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Order(Ordered.HIGHEST_PRECEDENCE) /* 设置最高优先级 */
public @interface LimitRequest {

    /**
     * 第一个等待时长，单位秒，默认60秒
     */
    long firstWaitTime() default 60;

    /**
     * 第二个等待时长，单位秒，默认300秒
     */
    long secondWatieTime() default 300;

    /**
     *  第三个等待时长，单位秒，默认600秒
     */
    long thirdWaitTime() default 600;

    /**
     *  极端等待时长，单位秒，默认3600秒
     */
    long ultraWaitTime() default 3600;

    /**
     *  在规定时间间隔内可请求多少次接口，默认3次
     */
    int limitRequestTime() default 3;

    /**
     *  时间间隔，单位分钟，默认5分钟
     */
    int timeInterval() default 5;

    /**
     *  被限制请求后返回的错误信息
     */
    String requestLimitedMsg() default "请求过于频繁";
}
