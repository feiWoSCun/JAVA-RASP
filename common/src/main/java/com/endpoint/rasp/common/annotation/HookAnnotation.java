package com.endpoint.rasp.common.annotation;

import java.lang.annotation.*;

/**
 * 包含HookAnnotation注解代表该类会被引擎加载，并进行插桩操作
 *
 * Created by yunchao.zheng on 2023-03-17
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HookAnnotation {
}
