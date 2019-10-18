package com.ten951.boot.mybatis.read;

import com.ten951.boot.mybatis.read.service.ITestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Darcy
 * @date 2019-09-11 17:19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestController {

    @Autowired
    private ITestService service;

    @Test
    public void test() {
        for (int i = 0; i < 10; i++) {
            service.insert(1L);
        }

    }
}
