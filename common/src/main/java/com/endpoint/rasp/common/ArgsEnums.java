package com.endpoint.rasp.common;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: feiwoscun
 * @date: 2024/6/13
 * @email: 2825097536@qq.com
 * @description:
 */
public enum ArgsEnums {

    ACTION("-action", true),
    PID("-pid", true),
    HOME("-home", true),
    IP("-ip", true),
    PORT("-port", true);
    private String k;
    private boolean need;

    ArgsEnums(String k, boolean need) {
        this.need = need;
        this.k = k;
    }

    ArgsEnums(String k) {
        this.k = k;
    }

    public String getK() {
        return k;
    }

    public boolean isNeed() {
        return need;
    }

    public static Map<String, Boolean> cache;

    static {
        cache = Arrays.stream(ArgsEnums.values()).collect(Collectors.toMap(ArgsEnums::getK, ArgsEnums::isNeed));
    }

    }
