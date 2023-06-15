package com.flip.config;

import com.flip.filter.JwtAuthenticationFilter;
import com.flip.handler.login.LogoutHandler;
import com.flip.handler.login.LogoutSuccessHandlerImpl;
import com.flip.handler.login.NoLoginHandler;
import com.flip.handler.login.NoPermissionHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${cors.allowed-address}")
    private String corsAllowedOrigin;

    @Resource
    private AuthenticationConfiguration authenticationConfiguration;

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Resource
    private LogoutHandler logoutHandler;

    @Resource
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private NoLoginHandler noLoginHandler;

    @Resource
    private NoPermissionHandler noPermissionHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); //设置为false时，loadUserByUsername中抛出的UsernameNotFoundException才能被全局捕获并处理

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_MODERATOR  > ROLE_USER");
        return roleHierarchy;
    }

    @Bean
    public DefaultWebSecurityExpressionHandler defaultExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }

    @Bean
    public SecurityFilterChain  securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers("/static/**", "/avatar/**").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/_checkUsernameUnique", "/_checkEmailUnique", "/_captcha", "/_checkCaptcha", "/_emailCode", "/_checkEmailVerificationCode").permitAll()
                .requestMatchers("/list", "/postInfo", "/getComments", "/getReplies", "/userProfile", "/userPosts", "/tags", "/tag", "/tagOption",
                        "/tagPosts", "/tagsAndOptions", "/search/post", "/search/user").permitAll()
                .requestMatchers("/sys-ctrl/tag", "/sys-ctrl/tagsAndOptions").permitAll()
                .requestMatchers("/sys-ctrl/**").authenticated()
                .anyRequest().authenticated();

        // 前后分离不需要表单登录
        http.formLogin().disable();

        // 设置Session生成策略为无状态，即不使用Session。使用Token需要配置这个
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 不使用session就不存在CSRF攻击，所以可以禁用CSRF
        http.csrf().disable();

        // CORS跨域
        http.cors().configurationSource(this.corsConfigurationSource());

        // 注入AuthenticationProvider
        http.authenticationProvider(authenticationProvider());

        // 遇到权限问题时的处理
        http.exceptionHandling()
                // 1. 未登录时的处理
                .authenticationEntryPoint(noLoginHandler)
                // 2. 权限不够时的处理
                .accessDeniedHandler(noPermissionHandler);

        // 自定义退出登录操作和成功退出登录操作
        http.logout().logoutUrl("/logout").addLogoutHandler(logoutHandler).logoutSuccessHandler(logoutSuccessHandler);

        //把token校验过滤器添加到过滤器链之前执行
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, LogoutFilter.class);

        return http.build();
    }

    /**
     * 配置CORS源
     */
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "HEAD", "PUT", "DELETE", "OPTIONS"));

        /*
            浏览器默认只返回这六个响应头：
            1）Cache-Control     2) Content-Language     3) Content-Type
            4) Expires           5) Last-Modified        6) Pragma
            想要将刷新后的 accessToken 附到 Response Header 上，需要暴露出这个响应头
        */
        corsConfiguration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsSource;
    }
}
