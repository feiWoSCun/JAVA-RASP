package com.endpoint.rasp.checker;

import com.endpoint.rasp.ScriptEngineNameFactory;

import javax.script.ScriptEngine;


/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class DefaultCheckChain implements CheckChain {
    private final Checker[] checkers;
    private final Object[] args;
    private final String method;
    private int pos = 0;
    private final int checkLen;


    private final ScriptEngine scriptEngine;

    public DefaultCheckChain(String method, Checker[] checkers, Object[] args, String engineName) {
        this.checkers = checkers;
        this.args = args;
        this.checkLen = checkers.length;
        this.method = method;
        this.scriptEngine = ScriptEngineNameFactory.doCreateEngine(engineName, args);
    }

    @Override
    public boolean doCheckChain() {
        if (pos >= checkLen) {
            return true;
        }

        Checker checker = checkers[pos++];
        return checker.check(args, method, this);
    }

    @Override
    public ScriptEngine getEngine() {
        return scriptEngine;
    }

    public Object[] getArgs() {
        return args;
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public Checker[] getCheckers() {
        return checkers;
    }
}
