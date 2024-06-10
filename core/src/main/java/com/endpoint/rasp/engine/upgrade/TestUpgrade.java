package com.endpoint.rasp.engine.upgrade;

import com.endpoint.rasp.engine.bootstrap.RaspBootstrap;

/**
 * 测试引擎升级
 * Created by yunchao.zheng on 2023-03-24
 */
public class TestUpgrade {
    /**
     * 周期性执行引擎更新
     */
    public static void upgradeCycle(final RaspBootstrap boot){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    try {
////                        boot.upgrade();
//                        TimeUnit.SECONDS.sleep(20);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }
}
