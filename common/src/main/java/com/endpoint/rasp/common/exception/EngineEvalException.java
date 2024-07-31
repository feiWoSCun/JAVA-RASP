package com.endpoint.rasp.common.exception;

/**
 * 加载配置时发生的错误
 * @author feiwoscun
 */
public class EngineEvalException extends RuntimeException {

    public EngineEvalException(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineEvalException(Throwable cause) {
        super(cause);
    }

    /**
     * constructor
     *
     * @param message 加载配置异常信息
     */
    public EngineEvalException(String message) {
        super(message);
    }

}
