package test;

import java.util.concurrent.TimeUnit;

/**
 * @author: feiwoscun
 * @date: 2024/8/5
 * @email: 2825097536@qq.com
 * @description:
 */
public class Test {
    public static void main(String[] args) {
        test();
    }

    private static void test() {
        Test test2 = new Test();
        System.out.println("feiwoscun server start");
        while (true) {
            System.out.println("调用开始");
            doTest1();
            doTest1("feiwoscun");
            test2.doTest();
            test2.doTest("feiwoscun");
            System.out.println("调用结束");
            try {
                TimeUnit.SECONDS.sleep(10);
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
