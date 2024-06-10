package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

import java.util.Map;

/**
 * 检测Servlet型内存马注入（覆盖Tomcat，Jetty，Resin，含哥斯拉）
 * Created by yunchao.zheng on 2023-07-27
 */
public class ServletShellChecker extends MemoryShellChecker {

    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        Map<String,Object> params = checkParameter.getParams();
        return this.checkServletShell(params);
    }


    /**
     * 检测Servlet型内存马注入（覆盖Tomcat，Jetty，Resin，含哥斯拉）
     *
     * @param params
     * @return
     */
    private EventInfo checkServletShell(Map<String,Object> params){
        //阻断告警级别更高写在前面,先检测哥斯拉
        if(params.containsKey("servletClassName")&&params.get("servletClassName")!=null&&isGodzillaClass((String)params.get("servletClassName"))){
            String className = (String)params.get("servletClassName");
            String classFilePath=params.get("servletClassFilePath")!=null?(String)params.get("servletClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_GODZILLA_SERVLET_WARNING,MemoryShellConstant.ACTION_BLOCK,className,classFilePath,stackTrace);
        }
        if(params.containsKey("servletClassFilePath")&&isDangerousClassFilePath(params.get("servletClassFilePath"))){
            String className = params.get("servletClassName")!=null?(String)params.get("servletClassName"):null;
            String classFilePath=params.get("servletClassFilePath")!=null?(String)params.get("servletClassFilePath"):null;
            String[] stackTrace = params.get("stackTrace")!=null?(String[]) params.get("stackTrace"):null;
            return new MemoryShellInfo(MemoryShellConstant.MEMORY_SHELL_SERVLET_WARNING,MemoryShellConstant.ACTION_WARNING,className,classFilePath,stackTrace);
        }
        return null;
    }
}
