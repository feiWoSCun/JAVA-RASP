package com.endpoint.rasp.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 获取栈信息工具类
 * <p>
 * Created by yunchao.zheng on 2023-03-20
 */
public class StackTrace {

    /**
     * 返回调用栈信息最大深度
     */
    private static final int pluginMaxStack = 10;


    /**
     * 获取栈信息
     *
     * @return 栈信息
     */
    public static String getStackTrace() {

        Throwable throwable = new Throwable();
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StringBuilder retStack = new StringBuilder();

        //此处前几个调用栈都是插件中产生的所以跳过，只显示服务本身的调用栈
        if (stackTraceElements.length >= 3) {
            for (int i = 2; i < stackTraceElements.length; i++) {
                retStack.append(stackTraceElements[i].getClassName() + "@" + stackTraceElements[i].getMethodName()
                        + "(" + stackTraceElements[i].getLineNumber() + ")" + "\r\n");
            }
        } else {
            for (int i = 0; i < stackTraceElements.length; i++) {
                retStack.append(stackTraceElements[i].getClassName() + "@" + stackTraceElements[i].getMethodName()
                        + "(" + stackTraceElements[i].getLineNumber() + ")" + "\r\n");
            }
        }

        return retStack.toString();
    }


    /**
     * 获取原始栈
     *
     * @return 原始栈
     */
    public static List<String> getStackTraceArray(boolean isFilter, boolean hasLineNumber) {
        LinkedList<String> stackTrace = new LinkedList<String>();
        Throwable throwable = new Throwable();
        StackTraceElement[] stack = throwable.getStackTrace();
        if (stack != null) {
            if (isFilter) {
                stack = filter(stack);
            }
            for (int i = 0; i < stack.length; i++) {
                if (hasLineNumber) {
                    stackTrace.add(stack[i].toString());
                } else {
                    stackTrace.add(stack[i].getClassName() + "." + stack[i].getMethodName());
                }
            }
        }

        return stackTrace;
    }

    /**
     * hook 点参数获取原始栈
     *
     * @return 原始栈
     */
    public static List<String> getParamStackTraceArray() {
        return getStackTraceArray(true, false);
    }

    /**
     * hook 点参数获取原始栈
     *
     * @return 原始栈
     */
    public static List<String> getFullStackTraceArray() {
        return getStackTraceArray(false, true);
    }

    //去掉包含rasp的堆栈
    public static StackTraceElement[] filter(StackTraceElement[] trace) {
        int i = 0;
        // 去除插件本身调用栈
        while (i < trace.length && (trace[i].getClassName().startsWith("com.endpoint.rasp")
                || trace[i].getClassName().contains("reflect"))) {
            i++;
        }
        return Arrays.copyOfRange(trace, i, Math.min(i + pluginMaxStack, trace.length));
    }

}
