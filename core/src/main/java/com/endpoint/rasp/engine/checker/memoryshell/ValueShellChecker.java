package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测Tomcat Value型内存马注入
 * Created by yunchao.zheng on 2023-07-27
 */
public class ValueShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkValueShell(params);
    }

    /**
     * 检测Tomcat Value型内存马注入
     *
     * @param params
     * @return
     */
    private EventInfo checkValueShell(Map<String,Object> params){
        if(params.containsKey("valveClassFilePath")&&isDangerousClassFilePath(params.get("valveClassFilePath"))){
            String className = params.get("valveName")!=null?(String)params.get("valveName"):null;
            String classFilePath = params.get("valveClassFilePath")!=null?(String)params.get("valveClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_TOMCAT_VALUE_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }
}
