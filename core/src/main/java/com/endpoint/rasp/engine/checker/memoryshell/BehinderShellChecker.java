package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 冰蝎
 * Created by yunchao.zheng on 2023-07-27
 */
public class BehinderShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkBehinderShell(params);
    }
    /**
     * 检测冰蝎内存马注入
     *
     * @param params
     * @return
     */
    private EventInfo checkBehinderShell(Map<String,Object> params){
        if(params.containsKey("detectedBehinderAttack")&&(Boolean) params.get("detectedBehinderAttack")){
            String className = params.get("className")!=null?(String)params.get("className"):null;
            String classFilePath= params.get("classFilePath")!=null?(String)params.get("classFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_BEHINDER_WARNING,MemoryShellConstant.ACTION_BLOCK,className,classFilePath,stackTrace);
        }
        return null;
    }
}
