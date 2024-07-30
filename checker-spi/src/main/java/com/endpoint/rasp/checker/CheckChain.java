package com.endpoint.rasp.checker;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public interface CheckChain {

    /**
     * 链式调用入口
     *
     * @return 如果想要继续往下链式调用，在Checker的实现check()调用checkChain.doCheckChain()
     * @see Checker#check(Object[], String, CheckChain)
     */
    boolean doCheckChain();


}
