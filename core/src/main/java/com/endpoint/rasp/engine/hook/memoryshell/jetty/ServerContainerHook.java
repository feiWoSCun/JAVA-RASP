package com.endpoint.rasp.engine.hook.memoryshell.jetty;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.common.annotation.HookAnnotation;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;
import com.endpoint.rasp.engine.common.tool.Reflection;
import com.endpoint.rasp.engine.common.tool.StackTrace;
import com.endpoint.rasp.engine.hook.HookHandler;
import com.endpoint.rasp.engine.hook.memoryshell.AbstractMRVHook;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import rpc.service.BaseService;

import java.io.IOException;
import java.util.HashMap;

/**
 * 检测Jetty WebSocket型内存马
 */
@HookAnnotation
public class ServerContainerHook extends AbstractMRVHook {

    @Override
    public boolean isClassMatched(String className) {
        return "org/eclipse/jetty/websocket/jsr356/server/ServerContainer".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(ServerContainerHook.class, "checkAddEndpoint", "$1", Object.class);
        insertBefore(ctClass, "addEndpoint", "(Lorg/eclipse/jetty/websocket/jsr356/server/ServerEndpointMetadata;)V", src);

    }

    public static void checkAddEndpoint(Object serverEndpointMetadata) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        String[] stackTrace = StackTrace.getStackTrace().split("\r\n");
        String path = (String) Reflection.invokeMethod(serverEndpointMetadata, "getPath", new Class[]{});
        String websocketEndpointClassname = serverEndpointMetadata.getClass().getName();
        params.put("addEndpointStackTrace", stackTrace);
        params.put("endpointPath", path);
        params.put("websocketEndpointClassname", websocketEndpointClassname);
        params.put("serverType", MemoryShellConstant.SERVER_TYPE_JETTY);
        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("websocketEndpointClassname"),(String) params.get("endpointPath"), MemoryShellConstant.MEMORY_SHELL_JETTY_TYPE)){
            return;
        }
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }
}
