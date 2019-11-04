package com.ten951.boot.mybatis.read.controller;

import java.util.concurrent.CountDownLatch;

/**
 * @author Darcy
 * @date 2019-10-23 10:55
 */
public class ThreadB implements Runnable {

    private CountDownLatch latch;

    public ThreadB(CountDownLatch latch) {
        this.latch = latch;
    }


    @Override
    public void run() {
        System.out.println("latch = TB执行了");
        latch.countDown();
    }
}
