package com.endpoint.rasp.checker;

import com.endpoint.rasp.ScriptEngineNameFactory;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;


/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class DefaultCheckChain implements CheckChain {
    private final Checker[] checkers;
    private final Object[] args;
    private final String key;
    private int pos = 0;
    private final int checkLen;
    private final List<Object> results;
    private final ScriptEngine scriptEngine;

    public DefaultCheckChain(String key, Checker[] checkers, Object[] args, String engineName) {
        this.checkers = checkers;
        this.args = args;
        this.checkLen = checkers.length;
        this.key = key;
        this.scriptEngine = ScriptEngineNameFactory.doCreateEngine(engineName, args);
        this.results = new ArrayList<>();
    }

    @Override
    public boolean doCheckChain() {
        if (pos >= checkLen) {
            return true;
        }

        Checker checker = checkers[pos++];
        return checker.check(args, key, this);

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

    @Override
    public List<Object> getResults() {
        return results;
    }

    public Checker[] getCheckers() {
        return checkers;
    }
}
