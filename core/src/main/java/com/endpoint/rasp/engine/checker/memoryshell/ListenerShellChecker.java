package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.List;
import java.util.Map;

/**
 * 检测Listener型内存马注入（覆盖Tomcat，Jetty）
 * Created by yunchao.zheng on 2023-07-27
 */
public class ListenerShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkListenersShell(params);
    }

    /**
     * 检测Listener型内存马注入（覆盖Tomcat，Jetty）
     *
     * @param params
     * @return
     */
    private EventInfo checkListenersShell(Map<String,Object> params){
        //单个新增监听器场景
        if(params.containsKey("listenerClassFilePath")&&isDangerousClassFilePath(params.get("listenerClassFilePath"))){
            String className = params.get("listenerClassName")!=null?(String)params.get("listenerClassName"):null;
            String classFilePath = params.get("listenerClassFilePath")!=null?(String)params.get("listenerClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_LISTENER_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        //批量新增监听器场景
        if(params.containsKey("listenersClassFilePath")&&params.get("listenersClassFilePath")!=null){
            List<String> listenersClassFilePath = (List<String>) params.get("listenersClassFilePath");
            List<String> listenersClassName = (List<String>) params.get("listenersClassName");
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            if(listenersClassFilePath.size()==listenersClassName.size()){
                for(int i=0; i< listenersClassFilePath.size(); i++){
                    if(isDangerousClassFilePath(listenersClassFilePath.get(i))){
                        String className = listenersClassName.get(i);
                        String classFilePath = listenersClassFilePath.get(i);
                        return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_LISTENER_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
                    }
                }
            }
        }
        return null;
    }
}
