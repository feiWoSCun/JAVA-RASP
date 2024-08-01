package com.endpoint.rasp.checker;

import com.endpoint.rasp.Rule;
import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class GenericChecker implements Checker {

    private final String method;
    private final Rule rule;

    public GenericChecker(String method, Rule rule) {
        this.method = method;
        this.rule = rule;
    }

    @Override
    public boolean check(Object[] args, String method, CheckChain checkChain) {
        //
        return isMatch(method) && defaultCheck1(checkChain) && checkChain.doCheckChain();
    }

    private boolean defaultCheck1(CheckChain checkChain) {
        //执行检查逻辑
        ScriptEngine engine = checkChain.getEngine();
        try {
            engine.eval(rule.getPattern());
        } catch (ScriptException e) {
            LogTool.error(ErrorType.RUNTIME_ERROR, "调用失败，将尝试执行调用链的下一个", e);
            return true;
        }
        return true;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public boolean isMatch(String method) {
        return this.method.equals(method);
    }


}
