package com.ten951.boot.mybatis.read;

import java.util.*;

/**
 * @author Darcy
 * @date 2019-11-08 10:51
 */
public class Test {


    public int climbStairs(int n) {
        if (n == 0) {
            return 1;
        }
        int[] dp = new int[n + 1];
        dp[1] = 1;
        dp[2] = 2;
        for (int i = 3; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    public String addStrings(String num1, String num2) {
        StringBuilder sb = new StringBuilder();
        int i = num1.length() - 1, j = num2.length() - 1, carry = 0;
        while (i >= 0 || j >= 0) {
            int n = i >= 0 ? num1.charAt(i) - '0' : 0;
            int m = j >= 0 ? num2.charAt(j) - '0' : 0;
            int temp = n + m + carry;
            carry = temp / 10;
            sb.append(temp % 10);
            i--;
            j--;
        }
        if (carry == 1) {
            sb.append(1);
        }
        return sb.reverse().toString();
    }

    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> sss = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int n = target - nums[i];
            if (sss.containsKey(n)) {
                return new int[]{sss.get(n), i};
            }
            sss.put(nums[i], i);
        }
        throw new IllegalArgumentException();
    }

    public boolean wordBreak(String s, List<String> wordDict) {
        Set<String> word = new HashSet<>(wordDict);
        boolean[] dp = new boolean[s.length() + 1];
        dp[0] = true;
        for (int i = 1; i < s.length(); i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && word.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[s.length()];
    }


    List<List<String>> list = new ArrayList<>();
    String s;

    public List<List<String>> partition1(String s) {
        //从头到尾递归+回溯。
        this.s = s;
        //这个是满足的每一个集合
        List<String> ll = new ArrayList<>();
        dfs(ll, 0);
        return list;
    }

    public void dfs(List<String> ll, int index) {
        if (index == s.length()) {
            list.add(new ArrayList<>(ll));
            return;
        }
        for (int i = index; i < s.length(); i++) {
            if (isPalindrome(index, i)) {
                ll.add(s.substring(index, i + 1));
                dfs(ll, i + 1);
                ll.remove(ll.size() - 1);
            }
        }
    }


    public Boolean isPalindrome(int start, int end) {
        while (start < end) {
            if (s.charAt(start) == s.charAt(end)) {
                start++;
                end--;
            } else {
                return Boolean.FALSE;
            }
        }
        return true;
    }


    public Boolean isPalindrome(String s) {
        if (s == null) {
            return false;
        }
        if (s.length() == 0) {
            return true;
        }
        int i = 0;
        int j = s.length() - 1;
        while (i < j) {
            while (i < j && !Character.isLetterOrDigit(s.charAt(i))) {
                i++;
            }
            while (i < j && !Character.isLetterOrDigit(s.charAt(j))) {
                j--;
            }
            if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j))) {
                return false;
            }
            i++;
            j--;
        }
        return true;
    }


    public int singleNumber(int[] nums) {
        int N = nums.length;
        int target = nums[0];
        for (int i = 1; i < N; i++) {
            target = target ^ nums[i];
        }

        return target;
    }

    public Integer majorityElement(int[] nums) {
        Map<Integer, Integer> c = new HashMap<>();
        for (int num : nums) {
            if (c.containsKey(num)) {
                c.put(num, c.get(num) + 1);
            } else {
                c.put(num, 1);
            }
        }

        return c.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);


    }


    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p1 = m - 1;
        int p2 = n - 1;
        int p = m + n - 1;
        while (p1 >= 0 && p2 >= 0) {
            if (nums1[p1] >= nums2[p2]) {
                nums1[p--] = nums1[p1--];
            } else {
                nums1[p--] = nums2[p2--];
            }
        }
        while (p2 >= 0) {
            nums1[p--] = nums2[p2--];
        }
    }


    public static void main(String[] args) {
        int i = 1 / 10;
        int i1 = 14 % 10;
        System.out.println("i1 = " + i1);
        List<List<String>> aab = new Test().partition1("aab");
        Boolean palindrome = new Test().isPalindrome("aaa");
        int[] nums1 = {0};
        int[] nums2 = {1};
        new Test().merge(nums1, 0, nums2, 1);
    }
}
