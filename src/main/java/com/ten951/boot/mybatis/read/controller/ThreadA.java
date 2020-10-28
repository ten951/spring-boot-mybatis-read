package com.ten951.boot.mybatis.read.controller;


import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Darcy
 * @date 2019-10-23 10:55
 */
public class ThreadA implements Runnable {
    private Main.Num count;
    private ReentrantLock lock;

    public ThreadA(Main.Num count, ReentrantLock lock) {
        this.count = count;
        this.lock = lock;
    }


    @Override
    public void run() {
        Integer add = count.add(1);
        System.out.println("i = " + count.getCount());

    }
}
