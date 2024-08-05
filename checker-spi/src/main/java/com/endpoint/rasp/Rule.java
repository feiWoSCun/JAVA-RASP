package com.endpoint.rasp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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
    @JsonProperty("argsIndex")
    private int[] argsIndex;

    @JsonProperty("desc")
    private String desc;

    public int[] getArgsIndex() {
        return argsIndex;
    }

    public void setArgsIndex(int[] argsIndex) {
        this.argsIndex = argsIndex;
    }


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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rule rule = (Rule) o;
        return ifStatic == rule.ifStatic && Objects.equals(className, rule.className) && Objects.equals(methodName, rule.methodName) && Objects.equals(bit, rule.bit) && Objects.deepEquals(argsIndex, rule.argsIndex) && Objects.equals(desc, rule.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, bit, ifStatic);
    }

    public String getKey() {
        return this.className + this.methodName + this.bit + this.ifStatic+this.desc;
    }
}
