package com.endpoint.rasp.engine.checker;

import com.endpoint.rasp.engine.checker.info.EventInfo;
import org.apache.log4j.Logger;

/**
 * Created by yunchao.zheng on 2023-03-20
 *
 * hook点检测接口
 */
public interface Checker {

    Logger POLICY_ALARM_LOGGER = Logger.getLogger(AbstractChecker.class.getPackage().getName() + ".policy_alarm");

    Logger ATTACK_ALARM_LOGGER = Logger.getLogger(AbstractChecker.class.getPackage().getName() + ".alarm");

    /**
     * 检测 hook 点参数
     *
     * @param parameter hook点参数
     * @return 是否阻塞 true代表安全 false代表危险
     */
    EventInfo check(CheckParameter parameter);

    /**
     * 设定检测器全局阻断控制，可以结合检测类型综合判定是否需要进行阻断
     *
     * @param canBlock
     */
    void setCanBlock(boolean canBlock);
}
