package com.endpoint.rasp.common;

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
