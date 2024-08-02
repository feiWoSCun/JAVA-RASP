package com.endpoint.rasp.engine.hook;


import com.endpoint.rasp.CheckerContext;
import com.endpoint.rasp.checker.CheckChain;
import com.endpoint.rasp.common.ArgsEnums;
import com.endpoint.rasp.common.exception.HookMethodException;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 检测Servlet、Listener、Filter内存马
 * 给Tomcat的Servlet、Listener、Filter可利用节点，添加HOOK检测点
 *
 * @author f
 */

public class GenerateContextHook extends AbstractMRVHook {

    private static final GenerateContextHook GENERATE_CONTEXT_HOOK = new GenerateContextHook();

    /**
     * 这个方法的参数弄的有点长了，应该封装成一个对象的
     *
     * @param ctClass
     * @param checkMethodName
     * @param methodName
     * @param argsIndex
     * @param ifStatic        是否静态方法
     * @return
     */
    public static byte[] doHook(CtClass ctClass, String checkMethodName, String methodName, int[] argsIndex, boolean ifStatic) {
        byte[] bytes = null;
        try {
            bytes = GENERATE_CONTEXT_HOOK.transformClass(ctClass, checkMethodName, methodName, argsIndex, ifStatic);
        } catch (NotFoundException | CannotCompileException e) {
            throw new HookMethodException(e);
        }
        return bytes;
    }

    @Override
    public boolean isClassMatched(String className) {
        return "org/apache/catalina/core/StandardContext".equals(className);
    }

    /**
     * @param ctClass         目标类
     * @param checkMethodName 目标方法内需要执行的方法
     * @param methodName      需要被hook的方法
     * @param argsIndex       方法的参数下标
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    @Override
    protected void hookMethod(CtClass ctClass, String checkMethodName, String methodName, int[] argsIndex, boolean ifStatic) throws CannotCompileException, NotFoundException {
        String src;

        src = getInvokeStaticSrc(GenerateContextHook.class, checkMethodName, generateStrings(argsIndex, ifStatic, methodName), String.class, Object[].class);
        insertBefore(ctClass, methodName, "(Ljava/lang/String;Ljava/lang/String;Z)V", src);
    }

    /**
     * @param methodName
     * @param args
     */
    public static void defaultCheckEnter(String methodName, Object... args) {
        CheckChain checkChain = CheckerContext.getCheckChain(methodName, args, GenerateContextHook.class.getClassLoader(), "groovy");
        boolean result = true;
        try {
            result = checkChain.doCheckChain();
        } catch (Exception ignored) {
        }
        if (result) {
            System.out.println("检测成功");
        }

    }

    /**
     * !静态方法需要拿到this 使用 $0
     *
     * @param argIndex
     * @param ifStatic
     * @return
     */
    private String generateStrings(int[] argIndex, boolean ifStatic, String methodName) {
        String params = null;
        if (ifStatic) {
            params = Arrays.stream(argIndex)
                    .mapToObj(i -> "$" + i)
                    .collect(Collectors.joining(","));
            if (!params.isEmpty()) {
                return "new Object[]{" + '"' + methodName + '"' + "," + params + "}";
            }
            return methodName;
        }
        params = Arrays.stream(argIndex)
                .mapToObj(i -> "$" + i)
                .collect(Collectors.joining(","));
        if (!params.isEmpty()) {
            return "new Object[]{" + '"' + methodName + '"' + "," + "new Object[]{" + params + "}}";
        }
        return methodName;
    }

}
