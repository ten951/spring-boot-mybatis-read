package com.ten951.boot.mybatis.read.controller;

/**
 * @author Darcy
 * @date 2019-12-13 11:00
 */
public enum MySingleton implements Singleton {
    INSTANCE {
        @Override
        public int doSomeThing(int u) {
            return 0;
        }
    };

    public static MySingleton getInstance() {
        return MySingleton.INSTANCE;
    }

}
