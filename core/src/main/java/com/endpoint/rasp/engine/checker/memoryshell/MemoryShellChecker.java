package com.endpoint.rasp.engine.checker.memoryshell;

import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.attack.AbstractAttackChecker;
import com.endpoint.rasp.engine.checker.info.EventInfo;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;
import com.endpoint.rasp.engine.common.log.LogTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存马检测器
 *
 * Created by yunchao.zheng on 2023-03-20
 */
public class MemoryShellChecker extends AbstractAttackChecker {
    /**
     * 内存马检测器集合
     */
    public static List<AbstractAttackChecker> checkers = new ArrayList<AbstractAttackChecker>();



    @Override
    public EventInfo checkParam(CheckParameter checkParameter) {
        LogTool.info("MemoryShellChecker Start Checking......");
        EventInfo info = null;
        for (AbstractAttackChecker checker:checkers) {
            info = checker.checkParam(checkParameter);
            if(info!=null){
                break;
            }
        }
        return info;
    }

    /**
     * 检测Class路径是否是异常路径。
     * 1、Class路径为空，即不存在Class文件；
     * 2、Class从JSP等目录加载；
     *
     * @param classFilePath
     * @return
     */
    public boolean isDangerousClassFilePath(Object classFilePath) {
        if (classFilePath == null) {
            return true;
        }
        String classPath = (String)classFilePath;
        for (int i = 0; i < MemoryShellConstant.DANGEROUS_CLASS_FILEPATHS.length; i++) {
            if (classPath.contains(MemoryShellConstant.DANGEROUS_CLASS_FILEPATHS[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测是否是哥斯拉内存马相关Class
     * @return
     */
    public boolean isGodzillaClass(String className){
        for (int i = 0; i < MemoryShellConstant.GODZILLA_CLASS_NAMES.length; i++) {
            if (className.equals(MemoryShellConstant.GODZILLA_CLASS_NAMES[i])) {
                return true;
            }
        }
        return false;
    }
    MemoryShellChecker(){
    }

    public MemoryShellChecker(List<AbstractAttackChecker> checker){
        checkers.add(new BehinderShellChecker());
        checkers.add(new ServletShellChecker());
        checkers.add(new FilterShellChecker());
        checkers.add(new ValueShellChecker());
        checkers.add(new ListenerShellChecker());
        checkers.add(new ExecutorShellChecker());
        checkers.add(new UpgradeShellChecker());
        checkers.add(new WebSocketShellChecker());
        checkers.add(new ControllerShellChecker());
        checkers.add(new InterceptorShellChecker());
    }

}
