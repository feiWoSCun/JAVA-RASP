package com.endpoint.rasp;

import com.endpoint.rasp.checker.Checker;

import java.util.ServiceLoader;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class CheckerServiceLoader {

    public static ServiceLoader<Checker> loadService(ClassLoader classLoader) {
        return ServiceLoader.load(Checker.class,classLoader);
    }
}
