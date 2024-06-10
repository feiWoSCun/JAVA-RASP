package com.endpoint.rasp.engine.hook.memoryshell;

import com.endpoint.rasp.engine.hook.AbstractClassHook;

public abstract class AbstractMRVHook extends AbstractClassHook {
    @Override
    public String getType() {
        return "memoryshell";
    }
}
