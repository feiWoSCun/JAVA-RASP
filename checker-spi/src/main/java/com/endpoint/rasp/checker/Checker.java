package com.endpoint.rasp.checker;

import javax.script.ScriptEngine;
import java.util.Set;
import java.util.function.Function;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public interface Checker {
    /**
     * 是否匹配
     *
     * @param method
     * @return
     */
    boolean isMatch(String method);

    /**
     * 通过方法名和方法参数校验
     *
     * @param args
     * @param method
     * @param checkChain
     * @return
     */
    boolean check(Object[] args, String method, CheckChain checkChain);


    default boolean defaultReturn(CheckChain checkChain) {
        return checkChain.doCheckChain();
    }

    default boolean defaultCheck(Object[] args, String method,
                                 Function<Object[], Boolean> function, CheckChain checkChain) {


        Boolean apply = function.apply(args);
        return isMatch(method) && apply && defaultReturn(checkChain);
    }

    String getMethods();

}
