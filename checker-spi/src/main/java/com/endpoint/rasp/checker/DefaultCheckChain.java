package com.endpoint.rasp.checker;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class DefaultCheckChain implements CheckChain {
    private final Checker[] checkers;
    private final Object[] args;
    private final String method;
    private int pos = 0;
    private final int checkLen;

    public DefaultCheckChain(String method, Checker[] checkers, Object[] args) {
        this.checkers = checkers;
        this.args = args;
        this.checkLen = checkers.length;
        this.method = method;
    }

    @Override
    public boolean doCheckChain() {
        if (pos >= checkLen) {
            return true;
        }

        Checker checker = checkers[pos++];
        return checker.check(args, method, this);
    }

    public Object[] getArgs() {
        return args;
    }

    public Checker[] getCheckers() {
        return checkers;
    }
}
