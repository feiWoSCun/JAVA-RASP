package com.endpoint.rasp.engine.common.exception;

/**
 * 扫描特定注解异常类
 *
 * Created by yunchao.zheng on 2023-03-17
 */
public class AnnotationScannerException extends RuntimeException {
    public AnnotationScannerException(Throwable cause) {
        super(cause);
    }
}
