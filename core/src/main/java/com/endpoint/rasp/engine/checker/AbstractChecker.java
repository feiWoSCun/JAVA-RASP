package com.endpoint.rasp.engine.checker;

import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;

/**
 * Created by yunchao.zheng on 2023-03-20
 *
 * hook点参数检测接口
 */
public abstract class AbstractChecker implements Checker {
    /**
     * 设定是否能够阻断，支持远程配置修改
     */
    private boolean canBlock = false;

    public AbstractChecker() {
        this(false);
    }

    public AbstractChecker(boolean canBlock) {
        this.canBlock = canBlock;
    }

    @Override
    public EventInfo check(CheckParameter checkParameter) {
        EventInfo info = checkParam(checkParameter);
        System.out.println(info.getMessage());
        boolean isBlock = false;
        //多条告警合并
        if (info!=null) {
//            if (info.checkAction()) {
//                info.setBlock(canBlock);
//            }
            //特例：Interceptor型内存马注入不能阻断，会影响业务
            if(MemoryShellConstant.MEMORY_SHELL_INTERCEPTOR_WARNING.equals(info.getMessage())){
                info.setBlock(false);
            }else{
                //全部交由平台开关控制，是否阻断
                info.setBlock(canBlock);
            }
        }
        return info;
    }

    public boolean isCanBlock() {
        return canBlock;
    }

    @Override
    public void setCanBlock(boolean canBlock) {
        this.canBlock = canBlock;
    }

    /**
     * 实现参数检测逻辑
     *
     * @param checkParameter 检测参数
     * @return 检测结果
     */
    public abstract EventInfo checkParam(CheckParameter checkParameter);

}
