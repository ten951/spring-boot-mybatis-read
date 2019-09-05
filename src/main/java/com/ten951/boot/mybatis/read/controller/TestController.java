package com.ten951.boot.mybatis.read.controller;

import com.ten951.boot.mybatis.read.entity.RepeatConfig;
import com.ten951.boot.mybatis.read.mapper.RepeatConfigMapper;
import com.ten951.boot.mybatis.read.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Darcy
 * @date 2019-08-27 13:33
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private ITestService testService;


    @RequestMapping(method = GET, path = "/get")
    public void tet(Long orderId) {
        testService.insert(orderId);
    }
}
