package com.endpoint.rasp.engine.hook.memoryshell.weblogic;

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
 * WebLogic Listener型内存马
 */
@HookAnnotation
public class EventsManagerHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "weblogic/servlet/internal/EventsManager".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(EventsManagerHook.class, "checkAddEventListener", "$1", Object.class);
        insertBefore(ctClass, "addEventListener", null, src);
    }

    public static void checkAddEventListener(Object listener) {
        if (listener != null) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("listenerClassFilePath", Reflection.getClassFilePath(listener.getClass()));
            params.put("listenerClassName", listener.getClass().getName());
            //效验白名单
            if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("listenerClassName"),(String) params.get("listenerClassFilePath"), MemoryShellConstant.MEMORY_SHELL_WEBLOGIC_TYPE)){
                return;
            }
            params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
            HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
        }
    }
}
