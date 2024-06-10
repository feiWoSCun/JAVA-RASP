package com.endpoint.rasp.engine.hook.memoryshell.tomcat;

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
 * Tomcat websocket 内存马检测
 */
@HookAnnotation
public class WsServerContainerHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/apache/tomcat/websocket/server/WsServerContainer".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(WsServerContainerHook.class, "checkAddEndpoint", "$1", Object.class);
        //低版本会提示找不到改方法，可忽略
        insertBefore(ctClass, "addEndpoint", "(Ljavax/websocket/server/ServerEndpointConfig;Z)V", src);
        //兼容低版本tomcat
        insertBefore(ctClass, "addEndpoint", "(Ljavax/websocket/server/ServerEndpointConfig;)V", src);
    }


    public static void checkAddEndpoint(Object serverEndpointConfig) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        String[] stackTrace = StackTrace.getStackTrace().split("\r\n");
        String path = (String) Reflection.invokeMethod(serverEndpointConfig, "getPath", new Class[]{});
        String websocketEndpointClassname = serverEndpointConfig.getClass().getName();
        params.put("addEndpointStackTrace", stackTrace);
        params.put("endpointPath", path);
        params.put("websocketEndpointClassname", websocketEndpointClassname);
        params.put("serverType", MemoryShellConstant.SERVER_TYPE_TOMCAT);
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("websocketEndpointClassname"),(String) params.get("endpointPath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
            return;
        }
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }
}
