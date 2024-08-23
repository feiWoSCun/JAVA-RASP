package com.endpoint.rasp.common.exception;

/**
 * 扫描特定注解异常类
 *
 * Created by yunchao.zheng on 2023-03-17
 */
public class HookMethodException extends RuntimeException {
    public HookMethodException(Throwable cause) {
        super(cause);
    }
}
