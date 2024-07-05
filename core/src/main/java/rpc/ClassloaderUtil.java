package rpc;

import rpc.service.ServiceStrategyHandler;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author: feiwoscun
 * @date: 2024/7/5
 * @email: 2825097536@qq.com
 * @description:
 */
public class ClassloaderUtil {
    public static ClassLoader getRaspClassLoader() {
        try {
            Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass("com.endpoint.rasp.agent.AgentBootstrap");
            Field raspClassLoader = aClass.getDeclaredField("raspClassLoader");

            return (ClassLoader) (raspClassLoader.get(null));
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ServiceStrategyHandler> getBeans() {
        Field f = null;
        Object o = null;
        try {
            f =getRaspClassLoader().loadClass("rpc.service.ServiceStrategyFactory").getDeclaredField("beans");
            f.setAccessible(true);
            o = f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return (List<ServiceStrategyHandler>)o;

    }
}
