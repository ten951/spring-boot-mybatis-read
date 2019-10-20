package com.ten951.boot.mybatis.read.controller;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Darcy
 * @date 2019-10-02 10:43
 */
public class Test {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);     // 创建一个ScheduledExecutorService实例

        final ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(new BeepTask(), 10, 10,
                TimeUnit.SECONDS);                              // 每隔10s蜂鸣一次
        scheduler.schedule(() -> {
            scheduledFuture.cancel(true);
        }, 1, TimeUnit.HOURS)  ;     // 1小时后, 取消蜂鸣任务
    }
    private static class BeepTask implements Runnable {
        @Override
        public void run() {
            System.out.println("beep!");
        }
    }
}
