package com.endpoint.rasp.engine.hook.memoryshell.spring;

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
import java.util.Iterator;
import java.util.List;

/**
 * Spring Interceptor 内存马
 */
@HookAnnotation
public class AbstractHandlerMappingHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/springframework/web/servlet/handler/AbstractHandlerMapping".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(AbstractHandlerMappingHook.class, "checkGetHandlerExecutionChain", "$0", Object.class);
        insertBefore(ctClass, "getHandlerExecutionChain", null, src);

    }

    public static void checkGetHandlerExecutionChain(Object o) {
        List<Object> adaptedInterceptors = null;
        try {
            adaptedInterceptors = (List<Object>) Reflection.findField(o, "adaptedInterceptors");
        } catch (Exception e) {
            return;
        }
        if (adaptedInterceptors != null) {
            Iterator<Object> iterator = adaptedInterceptors.iterator();
            while (iterator.hasNext()) {
                Object interceptor = iterator.next();
                if (Reflection.getClassFilePath(interceptor.getClass()) == null) {
                    try {
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("detectedDangerousInterceptor", true);
                        params.put("classPath",Reflection.getClassFilePath(interceptor.getClass()));
                        params.put("interceptorClassName", interceptor.getClass().getName());
                        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
                        //效验白名单
                        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("interceptorClassName"),(String) params.get("classPath"), MemoryShellConstant.MEMORY_SHELL_SPRING_TYPE)){
                            return;
                        }
                        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
                    } catch (SecurityException e) {
                        iterator.remove();
                        throw e;
                    }
                }
            }
        }
    }
}
