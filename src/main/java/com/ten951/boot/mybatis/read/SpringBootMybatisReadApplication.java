package com.ten951.boot.mybatis.read;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author yongtianwang
 */
@SpringBootApplication
@MapperScan(basePackages = "com.ten951.boot.mybatis.read.mapper")
@EnableTransactionManagement(mode = AdviceMode.PROXY)
public class SpringBootMybatisReadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMybatisReadApplication.class, args);
    }

}
