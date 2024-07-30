package com.endpoint.rasp.common;


import java.lang.reflect.Field;
import static com.endpoint.rasp.common.constant.RaspArgsConstant.COM_ENDPOINT_RASP_AGENT_SINGLETON_CLASSLOADER;

/**
 * @author: feiwoscun
 * @date: 2024/7/5
 * @email: 2825097536@qq.com
 * @description:
 */
public class ClassloaderUtil {


    public static ClassLoader getRaspClassLoader() {
        try {
            Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(COM_ENDPOINT_RASP_AGENT_SINGLETON_CLASSLOADER);
            Field raspClassLoader = aClass.getDeclaredField("raspClassLoader");

            return (ClassLoader) (raspClassLoader.get(null));
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
