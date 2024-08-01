package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测Tomcat Executor型内存马注入
 * Created by yunchao.zheng on 2023-07-27
 */
public class ExecutorShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkExecutorShell(params);
    }

    /**
     * 检测Tomcat Executor型内存马注入
     *
     * @param params
     * @return
     */
    private EventInfo checkExecutorShell(Map<String,Object> params){
        if(params.containsKey("executorClassFilePath")&&isDangerousClassFilePath(params.get("executorClassFilePath"))){
            String className = params.get("executorClassName")!=null?(String)params.get("executorClassName"):null;
            String classFilePath= params.get("executorClassFilePath")!=null?(String)params.get("executorClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_TOMCAT_EXECUTOR_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }

}
