package com.endpoint.rasp.engine.checker.info;

import com.google.gson.Gson;

import java.util.Map;

/**
 * 内存马注入事件信息
 * Created by yunchao.zheng on 2023-03-20
 */
public class MemoryShellInfo extends EventInfo {

    private Map params;
    /**
     * 调用栈追踪
     */
    private String[] stackTrace;
    /**
     * 类文件路径
     */
    private String classPath;
    /**
     * 类名称
     */
    private String className;

    public MemoryShellInfo(){}

    public MemoryShellInfo(String message, String action, Map params){
        super(message,action);
        this.params=params;
    }

    public MemoryShellInfo(String message, String action, String className,String classPath,String[] stackTrace){
        super(message,action);
        this.classPath=classPath;
        this.className=className;
        this.stackTrace=stackTrace;
    }

    public Map getParams() {
        return params;
    }

    public void setParams(Map params) {
        this.params = params;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
