package com.endpoint.rasp;

import com.endpoint.rasp.common.exception.ConfigLoadException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author: feiwoscun
 * @date: 2024/7/31
 * @email: 2825097536@qq.com
 * @description:
 */
public class ScriptEngineNameFactory {


    public static ScriptEngine doCreateEngine(String engineName, Object[] args) {
//莫名其妙解决了一个bug，添加一个自定义的类加载器就行了，why？
        ScriptEngineManager manager = new ScriptEngineManager(ScriptEngineNameFactory.class.getClassLoader());
        ScriptEngine engine = manager.getEngineByName(engineName);
        String param = "param";
        if (engine == null) {
            throw new ConfigLoadException("No script engine found for name " + engineName);
        } else {
            for (int i = 0; i < args.length; i++) {
                engine.put(param + i, args[i]);
            }
        }
        return engine;
    }
}
