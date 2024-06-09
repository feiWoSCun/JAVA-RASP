package com.endpoint.rasp.boot;

import java.util.Properties;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description:
 */
public class JavaVersionUtils {
    private static final String VERSION_PROP_NAME = "java.specification.version";
    private static final String JAVA_VERSION_STR = System.getProperty(VERSION_PROP_NAME);
    private static final float JAVA_VERSION = Float.parseFloat(JAVA_VERSION_STR);

    private JavaVersionUtils() {
    }

    public static String javaVersionStr() {
        return JAVA_VERSION_STR;
    }

    public static String javaVersionStr(Properties props) {
        return (null != props) ? props.getProperty(VERSION_PROP_NAME): null;
    }

    public static float javaVersion() {
        return JAVA_VERSION;
    }

    public static boolean isJava6() {
        return "1.6".equals(JAVA_VERSION_STR);
    }

    public static boolean isJava7() {
        return "1.7".equals(JAVA_VERSION_STR);
    }

    public static boolean isJava8() {
        return "1.8".equals(JAVA_VERSION_STR);
    }

    public static boolean isJava9() {
        return "9".equals(JAVA_VERSION_STR);
    }

    public static boolean isLessThanJava9() {
        return JAVA_VERSION < 9.0f;
    }

    public static boolean isGreaterThanJava7() {
        return JAVA_VERSION > 1.7f;
    }

    public static boolean isGreaterThanJava8() {
        return JAVA_VERSION > 1.8f;
    }

    public static boolean isGreaterThanJava11() {
        return JAVA_VERSION > 11.0f;
    }
}
