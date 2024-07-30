package com.endpoint.rasp.common.exception;

/**
 * Created by yunchao.zheng on 2023-06-30
 *
 * 用于做 hook 点拦截的运行时异常,避免与Java原生异常捕获冲突
 */
public class SecurityException extends RuntimeException {

    /**
     * (none-javadoc)
     *
     * @see RuntimeException#RuntimeException()
     */
    public SecurityException() {
        super();
    }

    /**
     * constructor
     *
     * @param message 安全异常信息
     */
    public SecurityException(String message) {
        super(message);
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }
}
