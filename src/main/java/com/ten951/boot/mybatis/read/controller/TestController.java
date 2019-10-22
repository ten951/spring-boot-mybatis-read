package com.ten951.boot.mybatis.read.controller;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.ten951.boot.mybatis.read.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.StringJoiner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Darcy
 * @date 2019-08-27 13:33
 */
@RestController
@RequestMapping("/test")
public class TestController implements Cloneable {
    private Integer i;
    private Integer b;

    private static final int capacity = 1000000;
    private static final int key = 999998;


    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), capacity);
    static {
        for (int i = 0; i < capacity; i++) {
            boolean put = bloomFilter.put(i);
        }
    }

    public TestController() {
        System.out.println("ssssssss");
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TestController.class.getSimpleName() + "[", "]")
                .add("i=" + i)
                .add("b=" + b)
                .toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Autowired
    private ITestService testService;

    public final void getName() {

    }


    public final void getName(String s) {

    }


    public static void main(String[] args) throws CloneNotSupportedException {
        /*int N = 5;  // 运动员数
        CyclicBarrier cb = new CyclicBarrier(N, new Runnable() {
            @Override
            public void run() {
                System.out.println("****** 所有运动员已准备完毕，发令枪：跑！******");
            }
        });

        for (int i = 0; i < N; i++) {
            Thread t = new Thread(new PrepareWork(cb), "运动员[" + i + "]");
            t.start();
        }*/

    }


    private static class PrepareWork implements Runnable {
        private CyclicBarrier cb;

        PrepareWork(CyclicBarrier cb) {
            this.cb = cb;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(500);
                System.out.println(Thread.currentThread().getName() + ": 准备完成");
                cb.await();          // 在栅栏等待
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }


    @RequestMapping(method = GET, path = "/get")
    public Object tet(Long orderId) {
       return testService.insert(orderId);
    }
}
