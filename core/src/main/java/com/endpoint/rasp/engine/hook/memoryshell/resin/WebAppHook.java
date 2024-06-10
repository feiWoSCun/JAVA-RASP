package com.endpoint.rasp.engine.hook.memoryshell.resin;

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
 * Resin Filter型内存马检测
 */
@HookAnnotation
public class WebAppHook extends AbstractMRVHook {

    @Override
    public boolean isClassMatched(String className) {
        return "com/caucho/server/webapp/WebApp".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src;
        src = getInvokeStaticSrc(WebAppHook.class, "checkAddFilter", "$1", Object.class);
        insertBefore(ctClass, "addFilter", "(Lcom/caucho/server/dispatch/FilterConfigImpl;)V", src);
        src = getInvokeStaticSrc(WebAppHook.class, "checkAddListenerObject", "$1",String.class);
        insertBefore(ctClass, "addListenerObject", null, src);
    }

    public static void checkAddFilter(Object config) {
        Class filterClass = (Class) Reflection.invokeMethod(config, "getFilterClass", new Class[]{});
        HashMap<String, Object> params = new HashMap<String, Object>();
        if (filterClass != null) {
            params.put("filterClassFilePath", Reflection.getClassFilePath(filterClass));
            params.put("filterClassName", filterClass.getName());
            params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
            //效验白名单
            if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("filterClassName"),(String) params.get("filterClassFilePath"), MemoryShellConstant.MEMORY_SHELL_RESIN_TYPE)){
                return;
            }
            HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
        }

    }

    public static void checkAddListenerObject(Object listenerObj) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("listenerClassFilePath", Reflection.getClassFilePath(listenerObj.getClass()));
        params.put("listenerClassName", listenerObj.getClass().getName());
        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("listenerClassName"),(String) params.get("listenerClassFilePath"), MemoryShellConstant.MEMORY_SHELL_RESIN_TYPE)){
            return;
        }
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }
}
