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
 * Tomcat Value 内存马检测
 */
@HookAnnotation
public class StandardPipelineHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/apache/catalina/core/StandardPipeline".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(StandardPipelineHook.class, "checkAddValve", "$1", Object.class);
        insertBefore(ctClass, "addValve", null, src);
    }

    public static void checkAddValve(Object valve) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("valveName", valve.getClass().getName());
        params.put("valveClassFilePath", Reflection.getClassFilePath(valve.getClass()));
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("valveName"),(String) params.get("valveClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
            return;
        }
        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }
}
