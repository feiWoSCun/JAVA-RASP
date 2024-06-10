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
/**
 * Spring Controller内存马
 */
@HookAnnotation
public class AbstractUrlHandlerMappingHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/springframework/web/servlet/handler/AbstractUrlHandlerMapping".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(AbstractUrlHandlerMappingHook.class, "checkRegisterHandler", "$0,$2", Object.class, Object.class);
        insertBefore(ctClass, "registerHandler", "(Ljava/lang/String;Ljava/lang/Object;)V", src);
    }

    public static void checkRegisterHandler(Object o, Object handler) {
        String controllerHandlerClassFilePath = null;
        HashMap<String, Object> params = new HashMap<String, Object>();
        if (handler instanceof String) {
            String handlerName = (String) handler;
            Object applicationContext = Reflection.invokeMethodWithSuperclass(o, "getApplicationContext", new Class[]{});
            if (applicationContext != null) {
                Class resolvedHandler = (Class) Reflection.invokeMethodWithSuperclass(applicationContext, "getType", new Class[]{String.class}, handlerName);
                if (resolvedHandler != null) {
                    controllerHandlerClassFilePath = Reflection.getClassFilePath(resolvedHandler);
                    params.put("controllerHandlerClassFilePath", controllerHandlerClassFilePath);
                    params.put("handlerClassName", resolvedHandler.getName());
                    params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
                    //效验白名单
                    if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("handlerClassName"),(String) params.get("controllerHandlerClassFilePath"), MemoryShellConstant.MEMORY_SHELL_SPRING_TYPE)){
                        return;
                    }
                    HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
                }
            }
        } else {
            controllerHandlerClassFilePath = Reflection.getClassFilePath(handler.getClass());
            params.put("controllerHandlerClassFilePath", controllerHandlerClassFilePath);
            params.put("handlerClassName", handler.getClass().getName());
            params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
            //效验白名单
            if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("handlerClassName"),(String) params.get("controllerHandlerClassFilePath"), MemoryShellConstant.MEMORY_SHELL_SPRING_TYPE)){
                return;
            }
            HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
        }
    }

}
