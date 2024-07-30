package com.endpoint.rasp.engine.hook;

public abstract class AbstractMRVHook extends AbstractClassHook {
    @Override
    public String getType() {
        return "memoryshell";
    }
}
