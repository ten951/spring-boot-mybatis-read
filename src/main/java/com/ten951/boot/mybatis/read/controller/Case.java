package com.ten951.boot.mybatis.read.controller;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Darcy
 * @date 2019-10-24 16:34
 */
public class Case {


    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newCachedThreadPool();
//        CyclicBarrier barrier = new CyclicBarrier(5);
        CyclicBarrier barrier = new CyclicBarrier(5, () -> {
            try {
                System.out.println("阶段完成，等待2秒...");
                Thread.sleep(2000);
                System.out.println("进入下个阶段...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

        executorService.submit(new StartGame("1", barrier));
        executorService.submit(new StartGame("2", barrier));
        executorService.submit(new StartGame("3", barrier));
        executorService.submit(new StartGame("4", barrier));
        executorService.submit(new StartGame("5", barrier));

        executorService.shutdown();

    }

}
