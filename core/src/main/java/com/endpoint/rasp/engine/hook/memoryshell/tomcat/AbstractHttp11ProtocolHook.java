package com.endpoint.rasp.engine.hook.memoryshell.tomcat;


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
import java.util.Map;

/**
 * Tomcat Upgrade型内存马
 */
@HookAnnotation
public class AbstractHttp11ProtocolHook extends AbstractMRVHook {
    @Override
    public boolean isClassMatched(String className) {
        return "org/apache/coyote/http11/AbstractHttp11Protocol".equals(className);
    }

    @Override
    protected void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        String src;
        src = getInvokeStaticSrc(AbstractHttp11ProtocolHook.class, "checkConfigureUpgradeProtocol", "$1", Object.class);
        insertBefore(ctClass, "configureUpgradeProtocol", null, src);
        src = getInvokeStaticSrc(AbstractHttp11ProtocolHook.class, "checkGetUpgradeProtocol", "$0,$1", Object.class, String.class);
        insertBefore(ctClass, "getUpgradeProtocol", null, src);
    }

    public static void checkConfigureUpgradeProtocol(Object upgradeProtocol) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("upgradeProtocolClassName", upgradeProtocol.getClass().getName());
        params.put("upgradeProtocolClassFilePath", Reflection.getClassFilePath(upgradeProtocol.getClass()));
        params.put("stackTrace", StackTrace.getStackTrace().split("\r\n"));
        //效验白名单
        if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("upgradeProtocolClassName"),(String) params.get("upgradeProtocolClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
            return;
        }
        HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
    }

    public static void checkGetUpgradeProtocol(Object http11Protocol, String upgradedName) {
        Map<String, Object> httpUpgradeProtocols = null;
        try {
            httpUpgradeProtocols = (Map<String, Object>) Reflection.findField(http11Protocol, "httpUpgradeProtocols");
        } catch (Exception e) {
            LogTool.warn(ErrorType.HOOK_ERROR,"Failed to  get httpUpgradeProtocols ", e);
            return;
        }
        if (httpUpgradeProtocols != null) {
            Object upgradeProtocol = httpUpgradeProtocols.get(upgradedName);
            if (upgradeProtocol != null) {
                String upgradeProtocolClassFilePath = Reflection.getClassFilePath(upgradeProtocol.getClass());
                if (upgradeProtocolClassFilePath != null) {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("upgradeProtocolClassName", upgradeProtocol.getClass().getName());
                    params.put("upgradeProtocolClassFilePath", upgradeProtocolClassFilePath);
                    //效验白名单
                    if(BaseService.getInstance().getRaspConfig().checkMemShellWhiteList((String) params.get("upgradeProtocolClassName"),(String) params.get("upgradeProtocolClassFilePath"), MemoryShellConstant.MEMORY_SHELL_TOMCAT_TYPE)){
                        return;
                    }
                    params.put("stackTrace",StackTrace.getStackTrace().split("\r\n"));
                    HookHandler.doCheck(CheckParameter.Type.MEMORYSHELL, params);
                }
            }
        }
    }

}

