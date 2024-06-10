package com.endpoint.rasp.engine.common.exception;

/**
 * Created by yunchao.zheng on 2023-03-17
 * 加载配置时发生的错误
 */
public class ConfigLoadException extends RuntimeException {

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigLoadException(Throwable cause) {
        super(cause);
    }

    /**
     * constructor
     *
     * @param message 加载配置异常信息
     */
    public ConfigLoadException(String message) {
        super(message);
    }

}
