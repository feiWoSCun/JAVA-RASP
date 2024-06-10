package com.endpoint.rasp.engine.checker;

import com.endpoint.rasp.engine.checker.attack.CommandAttackChecker;
import com.endpoint.rasp.engine.checker.memoryshell.MemoryShellChecker;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * 检测参数对象，包含与检测引擎的映射
 *
 * Created by yunchao.zheng on 2023-03-20
 */
public class CheckParameter {
    public static final HashMap<String, Object> EMPTY_MAP = new HashMap<String, Object>();

    /**
     * 检测类型枚举
     */
    public enum Type {
        COMMAND("command", new CommandAttackChecker(), 1 << 1),
        MEMORYSHELL("memoryshell", new MemoryShellChecker(null), 1 << 27),;

        /**
         * 检测类型
         */
        String name;
        /**
         * 检测器
         */
        Checker checker;

        Integer code;

        Type(String name, Checker checker, Integer code) {
            this.name = name;
            this.checker = checker;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public Checker getChecker() {
            return checker;
        }

        public Integer getCode() {
            return code;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Type type;
    private final Map params;
    private final long createTime;


    public CheckParameter(Type type, Map params) {
        this.type = type;
        this.params = params;
        this.createTime = System.currentTimeMillis();
    }

    public Object getParam(String key) {
        return params == null ? null : ((Map) params).get(key);
    }

    public Type getType() {
        return type;
    }

    public Map getParams() {
        return params;
    }

    public long getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        Map<String, Object> obj = new HashMap<String, Object>();
        obj.put("type", type);
        obj.put("params", params);
        return new Gson().toJson(obj);
    }
}
