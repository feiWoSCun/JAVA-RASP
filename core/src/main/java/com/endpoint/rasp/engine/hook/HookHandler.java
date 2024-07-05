package com.endpoint.rasp.engine.hook;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.CheckerManager;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.endpoint.rasp.engine.response.MemoryShellResponse;

import java.util.Map;

/**
 * HOOK事件处理器
 * Created by yunchao.zheng on 2023-03-20
 */
public class HookHandler {

    public static ThreadLocal<Boolean> enableCmdHook = ThreadLocal.withInitial(() -> true);

    /**
     * 实时检测入口
     *
     * @param type   检测类型
     * @param params 检测参数map，key为参数名，value为检测参数值
     */
    public static void doRealCheckWithoutRequest(CheckParameter.Type type, Map params) {
        EventInfo info = null;
        CheckParameter parameter = new CheckParameter(type, params);
        try {
            info = CheckerManager.check(type, parameter);
        } catch (Throwable e) {
            String msg = "plugin check error: " + e.getClass().getName() + " because: " + e.getMessage();
            LogTool.error(ErrorType.PLUGIN_ERROR, msg, e);
        }
        MemoryShellResponse.getInstance().doResponse(info, parameter);
    }


    /**
     * 无需在请求线程中执行的检测入口
     *
     * @param type   检测类型
     * @param params 检测参数map，key为参数名，value为检测参数值
     */
    public static void doCheckWithoutRequest(CheckParameter.Type type, Map params) {
        try {

            doRealCheckWithoutRequest(type, params);
        } catch (Throwable t) {
            LogTool.info(t.getMessage());
            if (t instanceof com.endpoint.rasp.engine.common.exception.SecurityException) {
                LogTool.warn("Throwing SecurityException, Message: " + t.getMessage());
                throw (com.endpoint.rasp.engine.common.exception.SecurityException) t;
            }
        }
    }

    /**
     * 请求线程检测入口
     *
     * @param type   检测类型
     * @param params 检测参数map，key为参数名，value为检测参数值
     */
    public static void doCheck(CheckParameter.Type type, Map params) {
        doCheckWithoutRequest(type, params);
    }
}