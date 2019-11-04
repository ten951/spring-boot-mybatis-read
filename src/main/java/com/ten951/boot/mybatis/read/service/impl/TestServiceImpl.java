package com.ten951.boot.mybatis.read.service.impl;

import com.ten951.boot.mybatis.read.entity.RepeatConfig;
import com.ten951.boot.mybatis.read.mapper.RepeatConfigMapper;
import com.ten951.boot.mybatis.read.service.ITestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Darcy
 * @date 2019-08-29 12:51
 */
@Slf4j
@Service
public class TestServiceImpl implements ITestService {

    @Autowired
    private RepeatConfigMapper configMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepeatConfig insert(Long i) {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RepeatConfig config = this.findById(i);
        log.info("{}", config);
        return config;

    }


    public RepeatConfig findById(Long i) {
        return configMapper.selectByPrimaryKey(i);

    }
}
