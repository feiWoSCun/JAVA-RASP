package com.endpoint.rasp.engine.checker;

import com.endpoint.rasp.common.LogTool;
import com.endpoint.rasp.engine.checker.info.EventInfo;


import java.util.EnumMap;

/**
 * Created by yunchao.zheng on 2023-03-20
 * <p>
 * 用于管理 hook 点参数的检测
 */
public class CheckerManager {

    private static EnumMap<CheckParameter.Type, Checker> checkers = new EnumMap<CheckParameter.Type, Checker>(CheckParameter.Type.class);

    /**
     * 检测引擎集合初始化
     */
    public synchronized static void init() {
        for (CheckParameter.Type type : CheckParameter.Type.values()) {
            LogTool.debug("CheckerManager init:" + type.getName() + " checkers:" + checkers.size());
            checkers.put(type, type.checker);
        }
    }

    /**
     * 修改内存马防护是否阻断
     *
     * @param canBlock 是否阻断
     */
    public static void updateBlockStatus(CheckParameter.Type type, boolean canBlock) {
        //清除之后会导致checkers是null
        if (CheckerManager.checkers == null) {
            checkers = new EnumMap<CheckParameter.Type, Checker>(CheckParameter.Type.class);
        }
        if (CheckerManager.checkers.size() == 0) {
            init();
        }
        CheckerManager.checkers.get(type).setCanBlock(canBlock);
    }

    /**
     * 清除所有检测引擎
     */
    public synchronized static void release() {
        checkers = null;
    }

    /**
     * 根据检测类型，获取对应的检测引擎，对检测参数执行检测
     *
     * @param type
     * @param parameter
     * @return
     */
    public static EventInfo check(CheckParameter.Type type, CheckParameter parameter) {
        //清除之后会导致checkers是null
        if (CheckerManager.checkers == null) {
            checkers = new EnumMap<CheckParameter.Type, Checker>(CheckParameter.Type.class);
        }
        //TODO 确认为何需要初始化多次，是否因为执行了多次Class Transform
        if (CheckerManager.checkers.isEmpty()) {
            init();
        }
        Checker checker = CheckerManager.checkers.get(type);
        return checker.check(parameter);
    }

}
