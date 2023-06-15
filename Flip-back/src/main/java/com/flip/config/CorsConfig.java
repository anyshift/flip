package com.flip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS跨域配置(Spring版)，该类不使用，改为通过 SpringSecurity 配置跨域。
 *
 * 给项目添加了 Spring Security 依赖之后，Spring 官方提供的三种跨域解决方案可能会失效。
 * 通过 `@CrossOrigin` 注解或者重写 `addCorsMappings` 方法配置跨域，都会失效。
 * 而通过 `CorsFilter` 配置的跨域，有没有失效要看过滤器的优先级。
 * 如果优先级高于 Spring Security 过滤器，即先于 Spring Security 执行，则 `CorsFilter` 所配置的跨域处理依然有效；
 * 如果优先级低于 Spring Security 过滤器，则 `CorsFilter` 所配置的跨域处理也会失效。
 */
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-address}")
    private String corsAllowedOrigin;

    /*  因为引入了 Spring Security，所以这里不再使用 Spring 提供的跨域配置，转而使用 Spring Security 的跨域配置。 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(corsAllowedOrigin)
                .allowCredentials(false)
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
