package com.endpoint.rasp.engine.hook.memoryshell.weblogic;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.common.annotation.HookAnnotation;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
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
 * WebLogic Filter型内存马
 */
@HookAnnotation
public class FilterManagerHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "weblogic/servlet/internal/FilterManager".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(FilterManagerHook.class, "checkRegisterFilter", "$2", String.class);
        insertBefore(ctClass, "registerFilter", null, src);
    }

    public static void checkRegisterFilter(String filterClassName) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        Class filterClass;
        try {
            filterClass = Thread.currentThread().getContextClassLoader().loadClass(filterClassName);
        } catch (Exception e) {
            LogTool.warn(ErrorType.HOOK_ERROR,"failed to get filter class", e);
            return;
        }
        params.put("filterClassFilePath", Reflection.getClassFilePath(filterClass));
        params.put("filterClassName",filterClass.getName());
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("filterClassName"),(String) params.get("filterClassFilePath"), MemoryShellConstant.MEMORY_SHELL_WEBLOGIC_TYPE)){
            return;
        }
        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);

    }
}
