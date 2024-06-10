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
 * WebLogic Servlet型内存马
 */
@HookAnnotation
public class ServletMappingHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "weblogic/servlet/utils/ServletMapping".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(ServletMappingHook.class, "checkPut", "$2", String.class);
        insertBefore(ctClass, "put", null, src);
    }

    public static void checkPut(Object value) {
        //Filter注入过程，会出现String对象作为put的入参，会出现异常
        if(value.getClass().getName().contains("URLMatchHelper")){
            Object servletStub = Reflection.invokeMethodWithSuperclass(value, "getServletStub", new Class[]{});
            if (servletStub != null) {
                Class servletClass = (Class) Reflection.findField(servletStub, "servletClass");
                if (servletClass != null) {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("servletClassFilePath", Reflection.getClassFilePath(servletClass));
                    params.put("servletClassName", servletClass.getName());
                    //效验白名单
                    if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("servletClassName"),(String) params.get("servletClassFilePath"), MemoryShellConstant.MEMORY_SHELL_WEBLOGIC_TYPE)){
                        return;
                    }
                    params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
                    HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
                }
            }
        }
    }
}
