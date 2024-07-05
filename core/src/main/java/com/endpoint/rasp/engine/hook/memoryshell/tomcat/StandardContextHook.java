package com.endpoint.rasp.engine.hook.memoryshell.tomcat;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 检测Servlet、Listener、Filter内存马
 * 给Tomcat的Servlet、Listener、Filter可利用节点，添加HOOK检测点
 */
@HookAnnotation
public class StandardContextHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/apache/catalina/core/StandardContext".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src;
        src = getInvokeStaticSrc(StandardContextHook.class, "checkAddApplicationEventListener", "$1", Object.class);
        insertBefore(ctClass, "addApplicationEventListener", null, src);

        src = getInvokeStaticSrc(StandardContextHook.class, "checkSetApplicationEventListeners", "$1", Object.class);
        insertBefore(ctClass, "setApplicationEventListeners", null, src);

        src = getInvokeStaticSrc(StandardContextHook.class, "checkAddServletMappingDecoded", "$0,$1,$2", Object.class, String.class, String.class);
        insertBefore(ctClass, "addServletMappingDecoded", "(Ljava/lang/String;Ljava/lang/String;Z)V", src);
        //部分版本不包含该方法
        insertBefore(ctClass, "addServletMapping", "(Ljava/lang/String;Ljava/lang/String;)V", src);

        src = getInvokeStaticSrc(StandardContextHook.class, "checkAddFilterDef", "$1", Object.class);
        insertBefore(ctClass, "addFilterDef", null, src);
    }

    /**
     * 检测利用监听器注入内存马
     * @param listener
     */
    public static void checkAddApplicationEventListener(Object listener) {
        if (listener != null) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("listenerClassFilePath", Reflection.getClassFilePath(listener.getClass()));
            params.put("listenerClassName", listener.getClass().getName());
            params.put("stackTrace",StackTrace.getStackTrace().split("\r\n"));
            //效验白名单
            if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("listenerClassName"),(String) params.get("listenerClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
                return;
            }
            HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
        }
    }
    /**
     * 检测利用监听器注入内存马
     * @param listeners
     */
    public static void checkSetApplicationEventListeners(Object[] listeners) {
        List<String> listenersClassFilePath = new ArrayList<String>();
        List<String> listenersClassName = new ArrayList<String>();
        if (listeners != null && listeners.length != 0) {
            for (int i = 0; i < listeners.length; i++) {
                String name = listeners[i].getClass().getName();
                String path = Reflection.getClassFilePath(listeners[i].getClass());
                //效验白名单
                if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList(name,path, MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
                    continue;
                }else{
                    listenersClassFilePath.add(path);
                    listenersClassName.add(name);
                }
            }
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("listenersClassFilePath", listenersClassFilePath);
            params.put("listenersClassName", listenersClassName);
            params.put("stackTrace",StackTrace.getStackTrace().split("\r\n"));
            HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
        }
    }

    /**
     * 检测利用Servlet注入内存马
     * @param standardContext
     * @param pattern
     * @param name
     */
    public static void checkAddServletMappingDecoded(Object standardContext, String pattern, String name) {
        Object wrapper = Reflection.invokeMethod(standardContext, "findChild", new Class[]{String.class}, name);
        if (wrapper != null) {
            Object servlet = Reflection.invokeMethod(wrapper, "getServlet", new Class[]{});
            if (servlet != null) {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("servletClassFilePath", Reflection.getClassFilePath(servlet.getClass()));
                params.put("servletClassName", servlet.getClass().getName());
                //效验白名单
                if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("servletClassName"),(String) params.get("servletClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
                    return;
                }
                params.put("stackTrace",StackTrace.getStackTrace().split("\r\n"));


                HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
            }
        }
    }

    /**
     * 检测利用过滤器添加内存马
     * @param filterDef
     */
    public static void checkAddFilterDef(Object filterDef) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        Object filter = null;
        String filterName = null;
        try {
            filter = Reflection.getField(filterDef, "filter");
            filterName = (String) Reflection.getField(filterDef, "filterName");
        } catch (Exception ignore) {
        }
        if (filter != null) {
            params.put("filterClassFilePath", Reflection.getClassFilePath(filter.getClass()));
            params.put("filterClassName", filter.getClass().getName());
            params.put("filterName", filterName);
        } else {
            String filterClassName = Reflection.invokeStringMethod(filterDef, "getFilterClass", new Class[]{});
            if (filterClassName != null) {
                try {
                    params.put("filterClassFilePath", Reflection.getClassFilePath(Thread.currentThread().getContextClassLoader().loadClass(filterClassName)));
                    params.put("filterClassName", filterClassName);
                    params.put("filterName", filterName);
                } catch (ClassNotFoundException e) {
                    params.put("filterClassFilePath",null);
                    params.put("filterClassName", filterClassName);
                    params.put("filterName", filterName);
                }
            }
        }
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("filterClassName"),(String) params.get("filterClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
            return;
        }
        params.put("stackTrace",StackTrace.getStackTrace().split("\r\n"));
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }
}
