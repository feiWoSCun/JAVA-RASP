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
 * Resin Servlet型内存马检测
 */
@HookAnnotation
public class ServletMappingHook extends AbstractMRVHook {

    @Override
    public boolean isClassMatched(String className) {
        return "com/caucho/server/dispatch/ServletMapping".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(ServletMappingHook.class, "checkInit", "$0", Object.class);
        insertBefore(ctClass, "init", null, src);
    }

    public static void checkInit(Object o) {
        Class servletClass = null;
        servletClass = (Class) Reflection.invokeMethodWithSuperclass(o, "getServletClass", new Class[]{});
        if (servletClass == null) {
            Object webApp;
            try {
                webApp = Reflection.findField(o, "_webApp");
                if (webApp == null) {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    Class servletInvocation = classLoader.loadClass("com.caucho.server.dispatch.ServletInvocation");
                    Object contextRequest = servletInvocation.getMethod("getContextRequest").invoke((Object) null);
                    webApp = Reflection.invokeMethodWithSuperclass(contextRequest, "getWebApp", new Class[]{});
                }

                String servletName = (String) Reflection.findField(o, "servletName");
                if (webApp != null && servletName != null) {
                    Object servletManager = Reflection.findField(webApp, "servletManager");
                    if (servletManager != null) {
                        Object servlet = Reflection.invokeMethodWithSuperclass(servletManager, "getServlet", new Class[]{String.class}, servletName);
                        if (servlet != null) {
                            servletClass = (Class) Reflection.findField(servlet, "_servletClass");
                        }
                    }
                }

            } catch (Exception ignore) {
            }
        }
        if (servletClass == null) {
            try {
                String servletClassName = (String) Reflection.invokeMethodWithSuperclass(o, "getServletClassName", new Class[]{});
                if (servletClassName != null) {
                    servletClass = Thread.currentThread().getContextClassLoader().loadClass(servletClassName);
                }
            } catch (Exception ignore) {
            }
        }

        if (servletClass != null) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("servletClassFilePath", Reflection.getClassFilePath(servletClass));
            params.put("servletClassName", servletClass.getName());
            params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
            //效验白名单
            if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("servletClassName"),(String) params.get("servletClassFilePath"), MemoryShellConstant.MEMORY_SHELL_RESIN_TYPE)){
                return;
            }
            HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
        }
    }
}
