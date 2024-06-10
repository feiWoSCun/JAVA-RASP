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
 * Resin WebSocket内存马检测
 */
@HookAnnotation
public class HttpServletRequestImplHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "com/caucho/server/http/HttpServletRequestImpl".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(HttpServletRequestImplHook.class, "checkStartWebSocket", "$1", Object.class);
        insertBefore(ctClass, "startWebSocket", null, src);
    }

    public static void checkStartWebSocket(Object listener) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("webSocketListeneClassFilePath", Reflection.getClassFilePath(listener.getClass()));
        params.put("webSocketListeneClassName", listener.getClass().getName());
        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("webSocketListeneClassName"),(String) params.get("webSocketListeneClassFilePath"), MemoryShellConstant.MEMORY_SHELL_RESIN_TYPE)){
            return;
        }
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }
}
