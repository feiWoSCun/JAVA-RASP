package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测WebSocket型内存马注入(TODO Tomcat和Jetty没有检测加载Class的路径，需要测试验证，是否能绕过)
 *
 * Created by yunchao.zheng on 2023-07-27
 */
public class WebSocketShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkWebSocketShell(params);
    }

    /**
     * 检测WebSocket型内存马注入(Tomcat和Jetty没有检测加载Class的路径，需要测试验证，是否能绕过)
     *
     * @param params
     * @return
     */
    private EventInfo checkWebSocketShell(Map<String,Object> params){
        //resin
        if(params.containsKey("webSocketListeneClassFilePath")&&isDangerousClassFilePath(params.get("webSocketListeneClassFilePath"))){
            String className = params.get("webSocketListeneClassName")!=null?(String)params.get("webSocketListeneClassName"):null;
            String classFilePath = params.get("webSocketListeneClassFilePath")!=null?(String)params.get("webSocketListeneClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_WEBSOCKET_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        //Tomcat and Jetty
        if(params.containsKey("addEndpointStackTrace")&&params.get("addEndpointStackTrace")!=null&&params.containsKey("serverType")&&params.get("serverType")!=null){
            String[] stackTrace = (String[]) params.get("addEndpointStackTrace");
            String whiteTrace = MemoryShellConstant.WEBSOCKET_WHITELIST.get(params.get("serverType"));
            if(whiteTrace!=null&&stackTrace.length>0){
                for(int i=0;i<stackTrace.length;i++){
                    if(whiteTrace.equals(stackTrace[i])){
                        return null;
                    }
                }
            }
            String className = params.get("websocketEndpointClassname")!=null?(String)params.get("websocketEndpointClassname"):null;
            String classFilePath = params.get("endpointPath")!=null?(String)params.get("endpointPath"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_WEBSOCKET_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }
}
