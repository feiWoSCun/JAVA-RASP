package com.endpoint.rasp.engine.hook;


import com.endpoint.rasp.common.annotation.HookAnnotation;
import com.endpoint.rasp.common.Reflection;
import com.endpoint.rasp.common.StackTrace;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.HashMap;


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

        src = getInvokeStaticSrc(StandardContextHook.class, "checkAddServletMappingDecoded", "$0,$1,$2", Object.class, String.class, String.class);
        insertBefore(ctClass, "addServletMappingDecoded", "(Ljava/lang/String;Ljava/lang/String;Z)V", src);
    }

    /**
     * 检测利用Servlet注入内存马
     *
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
/*                //效验白名单
                if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("servletClassName"),(String) params.get("servletClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
                    return;
                }*/
                params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));

                //todo  构建一个chain来链式调用
                //HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
            }
        }
    }


}
