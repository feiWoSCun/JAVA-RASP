package com.endpoint.rasp.checker;

import java.util.Set;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public abstract class GenericChecker implements Checker {

    private final Set<String> methods;

    public GenericChecker(Set<String> methods) {
        this.methods = methods;
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }

    @Override
    public boolean isMatch(String method) {
        return methods.contains(method);
    }

    @Override
    public boolean check(Object[] args, String method, CheckChain checkChain) {
        //执行检查逻辑

        //then
        return defaultCheck(args, method, (a) -> true, checkChain);
    }


}
