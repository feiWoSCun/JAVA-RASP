package com.endpoint.rasp;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        test();
    }

    private static void test() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        // The name usually has the format "pid@hostname".
        String pid = name.split("@")[0];
        System.out.println("Current process ID: " + pid);
        Main test2 = new Main();
        System.out.println("feiwoscun server start");
        while (true) {
            System.out.println("调用开始");
            doTest1();
            doTest1("feiwoscun");
            test2.doTest();
            test2.doTest("feiwoscun");
            System.out.println("调用结束");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void doTest(String s) {
        System.out.println("进入void doTest(String)");
    }

    public void doTest() {
        System.out.println("进入void doTest()");
    }

    public static void doTest1() {
        System.out.println("进入static void doTest1()");
    }

    public static void doTest1(String s) {
        System.out.println("进入static void doTest1(String)");
    }
}