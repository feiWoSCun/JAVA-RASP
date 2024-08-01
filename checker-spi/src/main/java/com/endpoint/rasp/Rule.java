package com.endpoint.rasp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author feiwoscun
 */
public class Rule {
    @JsonProperty("className")
    private String className;

    @JsonProperty("methodName")
    private String methodName;

    @JsonProperty("bit")
    private String bit;
    @JsonProperty("pattern")
    private String pattern;
    @JsonProperty("ifStatic")
    private boolean ifStatic;

    public boolean isIfStatic() {
        return ifStatic;
    }

    public void setIfStatic(boolean ifStatic) {
        this.ifStatic = ifStatic;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getBit() {
        return bit;
    }

    public void setBit(String bit) {
        this.bit = bit;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
