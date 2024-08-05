package com.endpoint.rasp.checker;

import com.endpoint.rasp.Rule;
import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class GenericChecker implements Checker {

    private final String key;
    private final Rule rule;

    public GenericChecker(String key, Rule rule) {
        this.key = key;
        this.rule = rule;
    }

    @Override
    public boolean check(Object[] args, String key, CheckChain checkChain) {
        //
        return isMatch(key) && defaultCheck1(checkChain) && checkChain.doCheckChain();
    }

    private boolean defaultCheck1(CheckChain checkChain) {
        //执行检查逻辑
        List<Object> results = checkChain.getResults();
        ScriptEngine engine = checkChain.getEngine();
        Object ret;
        try {
            ret = engine.eval(rule.getPattern());
            results.add(ret);
        } catch (Exception e) {
            //可以通过设置抛出不同的异常，来判断是什么内存马
            results.add(e);
            LogTool.error(ErrorType.RUNTIME_ERROR, "调用失败，将尝试执行调用链的下一个", e);
            return true;
        }
        return true;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean isMatch(String key) {
        return this.key.equals(key);
    }


}
