package com.ten951.boot.mybatis.read.controller;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Darcy
 * @date 2019-12-05 10:45
 */
public class SumThread implements Callable<Integer> {

    private List<Integer> list;

    private Integer lo;
    private Integer hi;

    public SumThread(List<Integer> list, Integer lo, Integer hi) {
        this.list = list;
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    public Integer call() throws Exception {
        Integer sum = 0;
        for (int i = lo; i < hi; i++) {
            System.out.println(list.get(i));
            sum += list.get(i);
        }

        return sum;
    }
}
