package com.ten951.boot.mybatis.read.service.impl;

import com.ten951.boot.mybatis.read.entity.RepeatConfig;
import com.ten951.boot.mybatis.read.mapper.RepeatConfigMapper;
import com.ten951.boot.mybatis.read.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Darcy
 * @date 2019-08-29 12:51
 */
@Service
public class TestServiceImpl implements ITestService {

    @Autowired
    private RepeatConfigMapper configMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(Long i) {
        RepeatConfig config = configMapper.selectByPrimaryKey(i);

    }
}
