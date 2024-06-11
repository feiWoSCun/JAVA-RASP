package com.endpoint.rasp.engine.response;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.checker.info.MemoryShellInfo;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import rpc.bean.RPCMemShellEventLog;
import rpc.job.SendRaspEventLogJob;

/**
 * 内存马响应器
 * Created by yunchao.zheng on 2023-07-28
 */
public class MemoryShellResponse {

    private static MemoryShellResponse instance = new MemoryShellResponse();

    private MemoryShellResponse() {
    }

    public static MemoryShellResponse getInstance() {
        return instance;
    }

    public  void doResponse(EventInfo info,CheckParameter parameter){

        //System.out.println(new ExceptionModel(ErrorType.ATTACK_MEMORY_SHELL_WARNNING, ErrorType.ATTACK_MEMORY_SHELL_WARNNING.getMessage() + ",detail:" + info.toString()));
        LogTool.warn(ErrorType.ATTACK_MEMORY_SHELL_WARNNING,ErrorType.ATTACK_MEMORY_SHELL_WARNNING.getMessage()+",detail:"+info.toString());
        if (info!=null) {
            //发送内存马告警
            if(info instanceof MemoryShellInfo){
                MemoryShellInfo memoryShellInfo = (MemoryShellInfo)info;
                SendRaspEventLogJob.addLog(new RPCMemShellEventLog(memoryShellInfo.isBlock(),memoryShellInfo.getMessage(),memoryShellInfo.getClassName(),memoryShellInfo.getClassPath(),memoryShellInfo.getStackTrace()));
            }
            if(info.isBlock()){
                //阻断响应
                handleBlock(parameter);
            }
        }
    }

    /**
     * 阻断执行,抛出SecurityException运行时异常
     *
     * @param parameter
     */
    private static void handleBlock(CheckParameter parameter) {
        com.endpoint.rasp.engine.common.exception.SecurityException securityException = new com.endpoint.rasp.engine.common.exception.SecurityException("Memory Shell Injection blocked by E-RASP");
        throw securityException;
    }
}
