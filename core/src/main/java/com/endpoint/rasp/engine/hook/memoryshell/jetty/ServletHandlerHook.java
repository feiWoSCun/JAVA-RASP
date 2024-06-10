package com.endpoint.rasp.engine.hook.memoryshell.jetty;

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
 * 检测Jetty Servlet&Filter型内存马
 */
@HookAnnotation
public class ServletHandlerHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/eclipse/jetty/servlet/ServletHandler".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(ServletHandlerHook.class, "checkSetServlets", "$1", Object.class);
        insertBefore(ctClass, "setServlets", null, src);
        src = getInvokeStaticSrc(ServletHandlerHook.class, "checkSetFilters", "$1", Object.class);
        insertBefore(ctClass, "setFilters", null, src);
    }

    public static void checkSetServlets(Object[] servletHolders) {
        if (servletHolders != null && servletHolders.length != 0) {
            String servletClassFilePath = null;
            String servletClassName = null;
            Object holder = servletHolders[servletHolders.length - 1];
            if (holder != null) {
                Class clazz = (Class) Reflection.findField(holder, "_class");
                if (clazz != null) {
                    servletClassFilePath = Reflection.getClassFilePath(clazz);
                } else {
                    Object heldClass = Reflection.invokeMethod(holder, "getHeldName", new Class[]{});
                    if (heldClass != null) {
                        servletClassFilePath = Reflection.getClassFilePath(heldClass.getClass());
                    } else {
                        servletClassName = Reflection.invokeStringMethod(holder, "getClassName", new Class[]{});
                        if (servletClassName != null) {
                            try {
                                servletClassFilePath = Reflection.getClassFilePath(Thread.currentThread().getContextClassLoader().loadClass(servletClassName));
                            } catch (ClassNotFoundException ignored) {
                            }
                        }
                    }
                }
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("servletClassFilePath", servletClassFilePath);
                params.put("servletClassName", servletClassName);
                params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
                //效验白名单
                if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("servletClassName"),(String) params.get("servletClassFilePath"), MemoryShellConstant.MEMORY_SHELL_JETTY_TYPE)){
                    return;
                }
                HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
            }
        }
    }

    public static void checkSetFilters(Object[] filterHolders) {
        if (filterHolders != null && filterHolders.length != 0) {
            String filterClassFilePath = null;
            String filterClassName = null;
            Object holder = filterHolders[filterHolders.length - 1];
            if (holder != null) {
                Class clazz = (Class) Reflection.findField(holder, "_class");
                if (clazz != null) {
                    filterClassFilePath = Reflection.getClassFilePath(clazz);
                } else {
                    Object heldClass = Reflection.invokeMethod(holder, "getHeldName", new Class[]{});
                    if (heldClass != null) {
                        filterClassFilePath = Reflection.getClassFilePath(heldClass.getClass());
                    } else {
                        filterClassName = Reflection.invokeStringMethod(holder, "getClassName", new Class[]{});
                        if (filterClassName != null) {
                            try {
                                filterClassFilePath = Reflection.getClassFilePath(Thread.currentThread().getContextClassLoader().loadClass(filterClassName));
                            } catch (ClassNotFoundException ignored) {
                            }
                        }
                    }
                }

                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("filterClassFilePath", filterClassFilePath);
                params.put("filterClassName", filterClassName);
                params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
                //效验白名单
                if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("filterClassName"),(String) params.get("filterClassFilePath"), MemoryShellConstant.MEMORY_SHELL_JETTY_TYPE)){
                    return;
                }
                HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
            }
        }
    }
}
