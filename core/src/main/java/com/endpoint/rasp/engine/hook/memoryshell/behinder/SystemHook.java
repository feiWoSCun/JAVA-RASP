package com.endpoint.rasp.engine.hook.memoryshell.behinder;

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
 * 冰蝎内存马注入检测
 */
@HookAnnotation
public class SystemHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "java/lang/System".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(SystemHook.class, "checkSetProperty", "$1,$2", String.class, String.class);
        insertBefore(ctClass, "setProperty", null, src);
    }

    public static void checkSetProperty(String key, String value) {
        if ("jdk.attach.allowAttachSelf".equals(key) && "true".equals(value)) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for (int i = 0; i < stackTraceElements.length; i++) {
                String methodName = stackTraceElements[i].getMethodName();
                if ("equals".equals(methodName)) {
                    if ("setProperty".equals(stackTraceElements[i - 1].getMethodName()) && "java.lang.System".equals(stackTraceElements[i - 1].getClassName())) {
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("detectedBehinderAttack", true);
                        //每次都是随机类名，如：com.nykd.Vdtztzknt，实际上都是MemShell.Class字节码。
                        params.put("className",stackTraceElements[i].getClassName());
                        params.put("classFilePath", Reflection.getClassFilePath(stackTraceElements[i].getClass()));
                        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
                        //效验白名单
                        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("className"),(String) params.get("classFilePath"),MemoryShellConstant.MEMORY_SHELL_BEHINDER_TYPE)){
                            continue;
                        }
                        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
                    }
                }
            }
        }
    }
}
