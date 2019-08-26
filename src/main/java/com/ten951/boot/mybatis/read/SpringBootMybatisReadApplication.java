package com.ten951.boot.mybatis.read;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author yongtianwang
 */
@SpringBootApplication
@EnableWebMvc
@MapperScan(basePackages = "com.ten951.boot.mybatis.read.mapper")
public class SpringBootMybatisReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMybatisReadApplication.class, args);
    }

}
