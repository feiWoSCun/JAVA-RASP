package com.endpoint.rasp.engine.checker.info;

import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

/**
 * 检测器检测结果信息
 *
 * Created by yunchao.zheng on 2023-03-20
 */
public class EventInfo {
    private boolean isBlock = false;
    private String message;
    private String action;
    public EventInfo(){}

    public EventInfo(String message, String action){
        super();
        this.message=message;
        this.action=action;
    }

    /**
     * 是否阻断
     *
     * @return
     */
    public boolean checkAction() {
        if(MemoryShellConstant.ACTION_BLOCK.equals(action)){
            return true;
        }else{
            return false;
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBlock(boolean block) {
        isBlock = block;
    }

    public boolean isBlock() {
        return isBlock;
    }

}
