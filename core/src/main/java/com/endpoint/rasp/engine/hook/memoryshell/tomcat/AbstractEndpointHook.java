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
 * Tomcat executor型内存马
 */
@HookAnnotation
public class AbstractEndpointHook extends AbstractMRVHook {

    @Override
    public boolean isClassMatched(String className) {
        return "org/apache/tomcat/util/net/AbstractEndpoint".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(AbstractEndpointHook.class, "checkSetExecutor", "$1", Object.class);
        insertBefore(ctClass, "setExecutor", null, src);
    }

    public static void checkSetExecutor(Object executor) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("executorClassFilePath", Reflection.getClassFilePath(executor.getClass()));
        params.put("executorClassName",executor.getClass().getName());
        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("executorClassName"),(String) params.get("executorClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
            return;
        }
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }
}
