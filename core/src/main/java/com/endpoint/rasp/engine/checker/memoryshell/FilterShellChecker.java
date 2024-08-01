package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测Filter型内存马注入（覆盖Tomcat，Jetty，Resin，含哥斯拉）
 * Created by yunchao.zheng on 2023-07-27
 */
public class FilterShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkFilterShell(params);
    }


    /**
     * 检测Filter型内存马注入（覆盖Tomcat，Jetty，Resin，含哥斯拉）
     *
     * @param params
     * @return
     */
    private EventInfo checkFilterShell(Map<String,Object> params){
        if(params.containsKey("filterName")&&params.get("filterName")!=null&&((String)params.get("filterName")).matches("^\\w*?\\d{10,13}$")&&isGodzillaClass((String)params.get("filterName"))){
            String className = params.get("filterName")!=null?(String)params.get("filterName"):null;
            String classFilePath=params.get("filterClassFilePath")!=null?(String)params.get("filterClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_GODZILLA_FILTER_WARNING,MemoryShellConstant.ACTION_BLOCK,className,classFilePath,stackTrace);
        }
        if(params.containsKey("filterClassFilePath")&&isDangerousClassFilePath(params.get("filterClassFilePath"))){
            String className = params.get("filterName")!=null?(String)params.get("filterName"):null;
            String classFilePath= params.get("filterClassFilePath")!=null?(String)params.get("filterClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_FILTER_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }

}
