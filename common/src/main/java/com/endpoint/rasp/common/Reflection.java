package com.endpoint.rasp.common;


import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * 反射工具类
 */
public class Reflection {
    private static final Logger LOGGER = Logger.getLogger(Reflection.class.getName());

    /**
     * 根据方法名调用对象的某一个方法
     *
     * @param object     调用方法的对象
     * @param methodName 方法名称
     * @param paramTypes 参数类型列表
     * @param parameters 参数列表
     * @return 方法返回值
     */
    public static Object invokeMethod(Object object, String methodName, Class[] paramTypes, Object... parameters) {
        if (object == null) {
            return null;
        }
        return invokeMethod(object, object.getClass(), methodName, paramTypes, parameters);
    }

    /**
     * 反射调用方法，并把返回值进行强制转换为String
     *
     * @return 被调用函数返回的String
     * @see #invokeMethod(Object, String, Class[], Object...)
     */
    public static String invokeStringMethod(Object object, String methodName, Class[] paramTypes, Object... parameters) {
        Object ret = invokeMethod(object, methodName, paramTypes, parameters);
        return ret != null ? (String) ret : null;
    }

    /**
     * 反射获取对象的字段包括私有的
     *
     * @param object    被提取字段的对象
     * @param fieldName 字段名称
     * @return 字段的值
     */
    public static Object getField(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    /**
     * 反射获取父类对象的字段包括私有的
     *
     * @param paramClass 被提取字段的对象
     * @param fieldName  字段名称
     * @return 字段的值
     */
    public static Object getSuperField(Object paramClass, String fieldName) {
        Object object = null;
        try {
            Field field = paramClass.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            object = field.get(paramClass);
        } catch (Exception e) {
            LogTool.traceError(ErrorType.RUNTIME_ERROR, e.getMessage(), e);
        }
        return object;
    }

    /**
     * 调用某一个类的静态方法
     *
     * @param className  类名
     * @param methodName 方法名称
     * @param paramTypes 参数类型列表
     * @param parameters 参数列表
     * @return 方法返回值
     */
    public static Object invokeStaticMethod(String className, String methodName, Class[] paramTypes, Object... parameters) {
        try {
            Class clazz = Class.forName(className);
            return invokeMethod(null, clazz, methodName, paramTypes, parameters);
        } catch (Exception e) {
            LogTool.traceError(ErrorType.RUNTIME_ERROR, "failed to invoke static method: " + e.getMessage(), e);
            return null;
        }
    }

    public static Object invokeMethod(Object object, Class clazz, String methodName, Class[] paramTypes, Object... parameters) {
        try {
            Method method = clazz.getMethod(methodName, paramTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(object, parameters);
        } catch (Exception e) {
            String message = "Reflection call " + methodName + " failed: " + e.getMessage();
            if (clazz != null) {
                message = "Reflection call " + clazz.getName() + "." + methodName + " failed: " + e.getMessage();
            }
            LogTool.traceError(ErrorType.RUNTIME_ERROR, message, e);
            return null;
        }
    }

    public static boolean isPrimitiveType(Object object) {
        try {
            return ((Class<?>) object.getClass().getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getClassFilePath(Class clazz) {
        if (clazz == null) {
            return null;
        }
        String className = clazz.getName();
        String classNamePath = className.replace(".", "/") + ".class";
        URL is = clazz.getClassLoader().getResource(classNamePath);
        if (is == null) {
            return null;
        } else {
            return is.getPath();
        }
    }


    public static Object findField(Object paramClass, String fieldName) {
        return findField(paramClass.getClass(), paramClass, fieldName);
    }


    public static Object findField(Class clazz, Object paramClass, String fieldName) {
        Object object = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            object = field.get(paramClass);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), paramClass, fieldName);
            } else {
                LogTool.traceError(ErrorType.RUNTIME_ERROR, e.getMessage(), e);
            }
        } catch (Exception e) {
            return null;
        }
        return object;
    }

    public static Object invokeMethodWithSuperclass(Object object, String methodName, Class[] paramTypes, Object... parameters) {
        return invokeMethodWithSuperclass(object, object.getClass(), methodName, paramTypes, parameters);
    }


    public static Object invokeMethodWithSuperclass(Object object, Class clazz, String methodName, Class[] paramTypes, Object... parameters) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(object, parameters);
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null) {
                return invokeMethodWithSuperclass(object, clazz.getSuperclass(), methodName, paramTypes, parameters);
            } else {
                String message = "Reflection call " + methodName + " failed: " + e.getMessage();
                if (object != null) {
                    message = "Reflection call " + object.getClass().getName() + "." + methodName + " failed: " + e.getMessage();
                }
                LogTool.traceError(ErrorType.RUNTIME_ERROR, message, e);
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
