package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测Spring Controller型内存马注入
 * Created by yunchao.zheng on 2023-07-27
 */
public class ControllerShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkControllerShell(params);
    }

    /**
     * 检测Spring Controller型内存马注入
     *
     * @param params
     * @return
     */
    private EventInfo checkControllerShell(Map<String,Object> params){
        if(params.containsKey("controllerHandlerClassFilePath")&&isDangerousClassFilePath(params.get("controllerHandlerClassFilePath"))){
            String className = params.get("handlerClassName")!=null?(String)params.get("handlerClassName"):null;
            String classFilePath= params.get("controllerHandlerClassFilePath")!=null?(String)params.get("controllerHandlerClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_CONTROLLER_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }
}
