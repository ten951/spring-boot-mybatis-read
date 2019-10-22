package com.ten951.boot.mybatis.read;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScans;

/**
 * @author yongtianwang
 */
@SpringBootApplication
@MapperScan(basePackages = "com.ten951.boot.mybatis.read.mapper")
public class SpringBootMybatisReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMybatisReadApplication.class, args);
    }

}
