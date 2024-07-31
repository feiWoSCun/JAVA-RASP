package com.endpoint.rasp;

import java.util.ServiceLoader;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class RuleProviderServiceLoader {

    public static ServiceLoader<RuleProvider> loadService(ClassLoader classLoader) {
        return ServiceLoader.load(RuleProvider.class,classLoader);
    }
}
