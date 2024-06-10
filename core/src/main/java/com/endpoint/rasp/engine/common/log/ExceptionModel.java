package com.endpoint.rasp.engine.common.log;

/**
 * 封装异常对象控制对象的输出
 *
 * Created by yunchao.zheng on 2023-03-17
 */
public class ExceptionModel {

    private String message;
    private ErrorType errorType;

    public ExceptionModel(ErrorType errorType, String message) {
        this.message = message;
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return "[E" + errorType.getCode() + "] " + message + ": ";
    }

}
