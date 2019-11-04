package com.ten951.boot.mybatis.read.controller;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Darcy
 * @date 2019-10-23 10:57
 */
public class Main {

   static ThreadLocal<Integer> local = new ThreadLocal<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Integer integer = local.get();

        ExecutorService executorService = Executors.newCachedThreadPool();
        ExecutorService executorService1 = Executors.newWorkStealingPool();
        //  CountDownLatch latch = new CountDownLatch(2);
        ReentrantLock lock = new ReentrantLock();
        ThreadA a = new ThreadA(new Num(), lock);
        /*ThreadA b = new ThreadA(latch);
        executorService.submit(a);
        executorService.submit(b);
        latch.await();*/
        for (int i = 0; i < 1000; i++) {
            executorService.submit(a);
        }

        System.out.println("主线程执行了");
        executorService.shutdown();

    }


    public static class  Num {
        private Integer count = 0;




        public Integer add(Integer i) {
            return count + i;
        }

        public Integer getCount() {
            return count;
        }
    }
}
