package com.ten951.boot.mybatis.read.controller;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Darcy
 * @date 2019-12-05 10:44
 */
public class Test1 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        SumThread sumThread = new SumThread(list, 0, 2);
        SumThread sumThread1 = new SumThread(list, 2, 5);
        SumThread sumThread2 = new SumThread(list, 5, list.size());
        Future<Integer> submit = executorService.submit(sumThread);
        Future<Integer> submit1 = executorService.submit(sumThread1);
        Future<Integer> submit2 = executorService.submit(sumThread2);
        System.out.println("submit = " + submit.get());
        System.out.println("submit1 = " + submit1.get());
        System.out.println("submit2 = " + submit2.get());
    }
}
