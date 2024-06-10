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
import java.util.HashMap;

/**
 * 哥斯拉 Tomcat Servlet型内存马，仅限于Tomcat6版本的注入，其他版本该类已删除
 */
@HookAnnotation
public class MapperHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/apache/tomcat/util/http/mapper/Mapper".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src = getInvokeStaticSrc(MapperHook.class, "checkAddWrapper", "$3", Object.class);
        insertBefore(ctClass, "addWrapper", "(Lorg/apache/tomcat/util/http/mapper/Mapper/Context;Ljava.lang.String;Ljava.lang.Object)V", src);
    }

    public static void checkAddWrapper(Object wrapper) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        if (wrapper != null) {
            Object servlet = Reflection.invokeMethodWithSuperclass(wrapper, "getServlet", new Class[]{});
            if (servlet != null) {
                params.put("servletClassFilePath", Reflection.getClassFilePath(servlet.getClass()));
                params.put("servletClassName", servlet.getClass().getName());
                //效验白名单
                if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("servletClassName"),(String) params.get("servletClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
                    return;
                }
                params.put("needCheckGodzilla", true);
                params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
                HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
            }
        }
    }
}
