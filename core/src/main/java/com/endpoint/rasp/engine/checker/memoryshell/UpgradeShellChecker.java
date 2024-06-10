package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测Tomcat Upgrade型内存马注入
 * Created by yunchao.zheng on 2023-07-27
 */
public class UpgradeShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkUpgradeShell(params);
    }

    /**
     * 检测Tomcat Upgrade型内存马注入
     *
     * @param params
     * @return
     */
    private EventInfo checkUpgradeShell(Map<String,Object> params){
        if(params.containsKey("upgradeProtocolClassFilePath")&&isDangerousClassFilePath(params.get("upgradeProtocolClassFilePath"))){
            String className = params.get("upgradeProtocolClassName")!=null?(String)params.get("upgradeProtocolClassName"):null;
            String classFilePath = params.get("upgradeProtocolClassFilePath")!=null?(String)params.get("upgradeProtocolClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_TOMCAT_UPGRADE_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }
}
