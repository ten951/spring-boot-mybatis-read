package com.ten951.boot.mybatis.read;

import com.ten951.boot.mybatis.read.entity.RepeatConfig;
import com.ten951.boot.mybatis.read.mapper.RepeatConfigMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootMybatisReadApplicationTests {

    @Autowired
    private RepeatConfigMapper repeatConfigMapper;

    @Test
    public void contextLoads() {
        RepeatConfig repeatConfig = repeatConfigMapper.selectByPrimaryKey(1L);
        System.out.println("repeatConfig = " + repeatConfig);
    }

}
