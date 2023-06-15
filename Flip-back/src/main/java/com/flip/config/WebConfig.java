package com.flip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.avatarPath}")
    private String avatarPath; /*上传的头像存储的地址*/

    @Value("${upload.avatarMapperPath}")
    private String avatarMapperPath; /*上传的头像映射的虚拟路径地址*/

    @Value("${upload.staticPath}")
    private String staticPath;

    @Value("${upload.staticMapperPath}")
    private String staticMapperPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(staticMapperPath + "**").addResourceLocations("file://" + staticPath);
        registry.addResourceHandler(avatarMapperPath + "**").addResourceLocations("file://" + avatarPath);
    }
}
