package com.endpoint.rasp.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: feiwoscun
 * @date: 2024/7/25
 * @email: 2825097536@qq.com
 * @description:
 */
public class SingletonClassloader {
    public static volatile ClassLoader raspClassLoader;

    /**
     *
     * @param inst
     * @param file
     * @return
     * @thr ows URISyntaxException
     * @throws MalformedURLException
     */
    public static ClassLoader getRaspClassLoader(Instrumentation inst,File file) throws URISyntaxException, MalformedURLException {
        if (raspClassLoader == null) {
            raspClassLoader = new RaspClassloader(new URL[]{file.toURI().toURL()});
        }
        return raspClassLoader;
    }
}
