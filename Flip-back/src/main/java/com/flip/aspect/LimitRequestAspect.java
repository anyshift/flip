package com.flip.aspect;

import cn.hutool.core.util.ObjectUtil;
import com.flip.annotation.LimitRequest;
import com.flip.domain.Response;
import com.flip.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 限制接口调用频率的切面类
 */
@Aspect
@Component
@Slf4j
public class LimitRequestAspect {

    private int limitedTime = 0; /* 已限制次数 */

    private final RedisTemplate<String, Object> redisTemplate;

    public LimitRequestAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 切点
     *
     * @param limitRequest 只要有该注解的方法就切入
     */
    @Pointcut("@annotation(limitRequest)")
    public void limitRequestPointCut(LimitRequest limitRequest) {
    }

    @Around(value = "limitRequestPointCut(limitRequest)", argNames = "joinPoint,limitRequest")
    public Object doAround(ProceedingJoinPoint joinPoint, LimitRequest limitRequest) throws Throwable {

        /* 因为只作用于方法上，所以转为MethodSignature */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        /* 获取AOP拦截下来的方法 */
        Method method = signature.getMethod();

        /* 获取添加在方法上的 @LimitRequest 注解对象 */
        LimitRequest limit = method.getAnnotation(LimitRequest.class);

        /* 获取AOP拦截下来的方法所在类的类名 */
        String className = signature.getDeclaringType().getSimpleName();

        /* 获取AOP拦截下来的方法名 */
        String methodName = signature.getName();

        /* 获取当前线程的请求 */
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtil.isNull(requestAttributes)) {
            return Response.error(500, "服务器发生错误");
        }

        /* 获取当前请求的IP */
        HttpServletRequest request = requestAttributes.getRequest();
        String IP = IpUtils.getIP(request);

        String key = methodName + "RequestedTime:" + IP;

        Integer requestedTime = (Integer) redisTemplate.opsForValue().get(key);

        int realRequestedTime = ObjectUtil.isNull(requestedTime) ? 1 : requestedTime;

        if (ObjectUtil.isNull(requestedTime)) {
            redisTemplate.opsForValue().set(key, realRequestedTime, limit.timeInterval(), TimeUnit.MINUTES);
        } else if (realRequestedTime < limit.limitRequestTime()) {
            redisTemplate.opsForValue().increment(key);
        } else if (realRequestedTime == limit.limitRequestTime()) {
            long duration = switchDuration(IP, limit, methodName);
            redisTemplate.opsForValue().set(key, realRequestedTime, duration, TimeUnit.SECONDS);
            return Response.error(400, limit.requestLimitedMsg());
        } else {
            redisTemplate.opsForValue().increment(key); /* 记录中的 value + 1，不更新TTL */
            return Response.error(400, limit.requestLimitedMsg());
        }

        return joinPoint.proceed();
    }


    /**
     * 根据已限制次数决定下一次限制的等待时长
     *
     * @param IP    请求者IP
     * @param limit 注解对象
     * @return 等待时长，单位秒
     */
    private long switchDuration(String IP, LimitRequest limit, String methodName) {
        String limitedTimeKey = methodName + "LimitedTime:" + IP;
        Integer limitedRequestTime = (Integer) redisTemplate.opsForValue().get(limitedTimeKey);
        if (ObjectUtil.isNull(limitedRequestTime)) {
            limitedTime = 1;

            int hour = LocalDateTime.now().getHour();
            long timeout;

            if (hour >= 3 && hour < 15) timeout = limit.firstWaitTime();
            else if (hour >= 15 && hour <= 19) timeout = limit.secondWatieTime();
            else timeout = limit.thirdWaitTime();

            redisTemplate.opsForValue().set(limitedTimeKey, limitedTime, timeout, TimeUnit.SECONDS);
            return limit.ultraWaitTime();
        } else {
            limitedTime++;
            redisTemplate.opsForValue().increment(limitedTimeKey);

            Long duration;
            if (limitedRequestTime >= 2) duration = limit.ultraWaitTime();
            else duration = redisTemplate.getExpire(limitedTimeKey);

            return ObjectUtil.isNull(duration) ? limit.ultraWaitTime() : duration.intValue();
        }
    }
}
