package com.ten951.boot.mybatis.read.controller;

import java.util.*;

/**
 * 阿里面试题
 *
 * @author Darcy
 * @date 2019-11-18 19:03
 */
public class AliInterview {


    public AliInterview() {
    }

    /**
     * 获取目标数组第k大的数(当目标数组去重后的数量<k. 则返回最大数)
     * 用HashSet去重后, 保持小顶堆中数据个数为k. 那么第k大, 就是小顶堆堆顶
     *
     * @param nums 目标数组(无序, 重复)
     * @param k    排名
     * @return 第k大的数. (当目标数组去重后的数量<k. 则返回最大的那个数). null: nums数组中不存在数字.
     */
    public Integer rank(Integer[] nums, Integer k) {
        /*目标数组为空 无意义*/
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException("nums数组不能为空");
        }
        /*k为空或者小于等于0 无意义*/
        if (k == null || k <= 0) {
            throw new IllegalArgumentException("k的值不能小于等于0");
        }
        Set<Integer> set = new HashSet<>(Arrays.asList(nums));
        /*小顶堆*/
        PriorityQueue<Integer> q = new PriorityQueue<>();
        for (Integer num : set) {
            if (num == null) {
                continue;
            }
            if (q.size() < k) {
                q.add(num);
            } else if (num > q.peek()) {
                q.poll();
                q.add(num);
            }
        }
        return q.peek();
    }


    /**
     * 排除第k大以外的数, 剩余数组的中位数
     *
     * @param nums 目标数组(无序且重复)
     * @param k    第k大
     * @return 中位数 null: 不存在中位数
     */
    public Double findMid(Integer[] nums, Integer k) {
        Integer rank = this.rank(nums, k);
        PriorityQueue<Integer> minQueue = new PriorityQueue<>();
        PriorityQueue<Integer> maxQueue = new PriorityQueue<>(Comparator.reverseOrder());
        int count = 0;
        for (Integer num : nums) {
            if (num == null) {
                continue;
            }
            if (num < rank) {
                count += 1;
                maxQueue.add(num);
                minQueue.add(maxQueue.poll());
                if ((count & 1) != 0) {
                    maxQueue.add(minQueue.poll());
                }
            }
        }
        if ((count & 1) == 0) {
            if (maxQueue.size() > 0 && minQueue.size() > 0) {
                return (maxQueue.peek().doubleValue() + minQueue.peek().doubleValue()) / 2;
            }
        } else {
            if (maxQueue.size() > 0) {
                return maxQueue.peek().doubleValue();
            }
        }
        return null;
    }


    public static void main(String[] args) {
        Integer[] nums = new Integer[]{5,4,6,11,1,2};
        AliInterview interview = new AliInterview();
        Integer rank = interview.rank(nums, 2);
        System.out.println("rank = " + rank);
        Double mid = interview.findMid(nums, 2);
        System.out.println("mid = " + mid);
    }
}
