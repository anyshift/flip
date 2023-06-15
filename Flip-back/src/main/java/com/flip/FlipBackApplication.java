package com.flip;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.flip.mapper")
@EnableAspectJAutoProxy
public class FlipBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlipBackApplication.class, args);
    }

}
