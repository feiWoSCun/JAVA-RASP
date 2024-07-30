package com.endpoint.rasp.common;

import org.apache.log4j.Logger;

/**
 * 日志记录工具
 */


/**
 * 日志记录工具
 */
public class LogTool {

    public static Logger LOGGER = Logger.getLogger(LogTool.class);

    public static void traceWarn(ErrorType errorType, String message, Throwable t) {
        warn(errorType, message, t);
    }

    public static void traceError(ErrorType errorType, String message, Throwable t) {
        error(errorType, message, t);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }
    public static void info(String message) {
        LOGGER.info(message);
    }
    public static void warn(String message) {
        LOGGER.warn(message);
    }
    public static void warn(ErrorType errorType, String message, Throwable t) {
        LOGGER.warn(new ExceptionModel(errorType, message), t);
    }

    public static void error(ErrorType errorType, String message, Throwable t) {
        LOGGER.error(new ExceptionModel(errorType, message), t);
    }

    public static void warn(ErrorType errorType, String message) {
        LOGGER.warn(new ExceptionModel(errorType, message));
    }

    public static void error(ErrorType errorType, String message) {
        LOGGER.error(new ExceptionModel(errorType, message));
    }


}
