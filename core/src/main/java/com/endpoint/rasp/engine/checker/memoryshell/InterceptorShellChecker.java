package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测Spring Interceptor型内存马注入
 * Created by yunchao.zheng on 2023-07-27
 */
public class InterceptorShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkInterceptorShell(params);
    }

    /**
     * 检测Spring Interceptor型内存马注入
     *
     * @param params
     * @return
     */
    private EventInfo checkInterceptorShell(Map<String,Object> params){
        if(params.containsKey("detectedDangerousInterceptor")&&(Boolean) params.get("detectedDangerousInterceptor")){
            String className = params.get("interceptorClassName")!=null?(String)params.get("interceptorClassName"):null;
            String classFilePath = params.get("classPath")!=null?(String)params.get("classPath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_INTERCEPTOR_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }
}
