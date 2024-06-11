package com.endpoint.rasp.engine.hook;

import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import javassist.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

/**
 * 用于向特定类的固定方法加钩子的抽象类
 * <p>
 * Created by yunchao.zheng on 2023-03-14
 */
public abstract class AbstractClassHook {
    private static final Logger LOGGER = Logger.getLogger(AbstractClassHook.class.getName());

    protected boolean couldIgnore = true;

    private boolean isLoadedByBootstrapLoader = false;

    /**
     * 用于判断类名与当前需要hook的类是否相同
     *
     * @param className 用于匹配的类名
     * @return 是否匹配
     */
    public abstract boolean isClassMatched(String className);

    /**
     * hook点所属检测类型．
     *
     * @return 检测类型
     */
    public abstract String getType();

    /**
     * hook 目标类的函数
     *
     * @param ctClass 目标类
     */
    protected abstract void hookMethod(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException;

    /**
     * 转化目标类
     *
     * @param ctClass 待转化的类
     * @return 转化之后类的字节码数组
     */
    public byte[] transformClass(CtClass ctClass) {
        try {
            hookMethod(ctClass);
            return ctClass.toBytecode();
        } catch (Throwable e) {
            LOGGER.info("transform class " + ctClass.getName() + " failed", e);
        }
        return null;
    }

    /**
     * 是否可以在 hook.ignore 配置项中被忽略
     *
     * @return hook 点是否可别忽略
     */
    public boolean couldIgnore() {
        return couldIgnore;
    }

    /**
     * hook 点所在的类是否被 BootstrapClassLoader 加载
     *
     * @return true 代表是
     */
    public boolean isLoadedByBootstrapLoader() {
        return isLoadedByBootstrapLoader;
    }

    /**
     * 设置 hook 点所在的类是否被 BootstrapClassLoader 加载
     *
     * @param loadedByBootstrapLoader true 代表是
     */
    public void setLoadedByBootstrapLoader(boolean loadedByBootstrapLoader) {
        isLoadedByBootstrapLoader = loadedByBootstrapLoader;
    }

    /**
     * 在目标类的目标方法的入口插入相应的源代码
     *
     * @param ctClass    目标类
     * @param methodName 目标方法名称
     * @param desc       目标方法的描述符号
     * @param src        待插入的源代码
     */
    public void insertBefore(CtClass ctClass, String methodName, String desc, String src)
            throws NotFoundException, CannotCompileException {

        LinkedList<CtBehavior> methods = getMethod(ctClass, methodName, desc, null);
        if (methods != null && methods.size() > 0) {
            insertBefore(methods, src);
        } else {
            LOGGER.info("can not find method " + methodName + " " + desc + " in class " + ctClass.getName());
        }

    }

    /**
     * 在目标类的目标方法的入口插入相应的源代码
     * 可排除一定的方法
     *
     * @param ctClass     目标类
     * @param methodName  目标方法名称
     * @param excludeDesc 排除的方法描述符
     * @param src         待插入的源代码
     */
    public void insertBeforeWithExclude(CtClass ctClass, String methodName, String excludeDesc, String src)
            throws NotFoundException, CannotCompileException {

        LinkedList<CtBehavior> methods = getMethod(ctClass, methodName, null, excludeDesc);
        if (methods != null && methods.size() > 0) {
            insertBefore(methods, src);
        } else {
            LOGGER.info("can not find method " + methodName +
                    " exclude desc:" + excludeDesc + " in class " + ctClass.getName());
        }

    }

    private void insertBefore(LinkedList<CtBehavior> methods, String src)
            throws CannotCompileException {
        for (CtBehavior method : methods) {
            if (method != null) {
                insertBefore(method, src);
            }
        }
    }

    /**
     * 在目标类的一组重载的目标方法的入口插入相应的源代码
     *
     * @param ctClass    目标类
     * @param methodName 目标方法名称
     * @param allDesc    目标方法的一组描述符
     * @param src        待插入的源代码
     */
    public void insertBefore(CtClass ctClass, String methodName, String src, String[] allDesc)
            throws NotFoundException, CannotCompileException {
        for (String desc : allDesc) {
            insertBefore(ctClass, methodName, desc, src);
        }
    }

    /**
     * 在目标类的目标方法的出口插入相应的源代码
     *
     * @param ctClass    目标类
     * @param methodName 目标方法名称
     * @param desc       目标方法的描述符号
     * @param src        待插入的源代码
     * @param asFinally  是否在抛出异常的时候同样执行该源代码
     */
    public void insertAfter(CtClass ctClass, String methodName, String desc, String src, boolean asFinally)
            throws NotFoundException, CannotCompileException {

        LinkedList<CtBehavior> methods = getMethod(ctClass, methodName, desc, null);
        if (methods != null && !methods.isEmpty()) {
            for (CtBehavior method : methods) {
                if (method != null) {
                    insertAfter(method, src, asFinally);
                }
            }
        } else {
            LOGGER.info("can not find method " + methodName + " " + desc + " in class " + ctClass.getName());
        }

    }

    private LinkedList<CtBehavior> getConstructor(CtClass ctClass, String desc) {
        LinkedList<CtBehavior> methods = new LinkedList<CtBehavior>();
        if (desc != null && !desc.isEmpty()) {
            Collections.addAll(methods, ctClass.getDeclaredConstructors());
        } else {
            try {
                methods.add(ctClass.getConstructor(desc));
            } catch (NotFoundException e) {
                // ignore
            }
        }
        return methods;
    }

    /**
     * 获取特定类的方法实例
     * 如果描述符为空，那么返回所有同名的方法
     *
     * @param ctClass    javassist 类实例
     * @param methodName 方法名称
     * @param desc       方法描述符
     * @return 所有符合要求的方法实例
     * @see javassist.bytecode.Descriptor
     */
    protected LinkedList<CtBehavior> getMethod(CtClass ctClass, String methodName, String desc, String excludeDesc) {
        if ("<init>".equals(methodName)) {
            return getConstructor(ctClass, desc);
        }
        LinkedList<CtBehavior> methods = new LinkedList<CtBehavior>();
        if (desc==null||desc.isEmpty()) {
            CtMethod[] allMethods = ctClass.getDeclaredMethods();
            if (allMethods != null) {
                for (CtMethod method : allMethods) {
                    if (method != null
                            && !method.isEmpty()
                            && method.getName().equals(methodName)
                            && !method.getSignature().equals(excludeDesc)) {
                        methods.add(method);
                    }
                }
            }
        } else {
            try {
                CtMethod ctMethod = ctClass.getMethod(methodName, desc);
                if (ctMethod != null && !ctMethod.isEmpty()) {
                    methods.add(ctMethod);
                }
            } catch (NotFoundException e) {
                // ignore
            }
        }
        return methods;
    }

    /**
     * 在目标类的目标方法的入口插入相应的源代码
     *
     * @param method 目标方法
     * @param src    源代码
     */
    public void insertBefore(CtBehavior method, String src) throws CannotCompileException {
        try {
            method.insertBefore(src);
            LOGGER.debug("insert before method " + method.getLongName());
        } catch (CannotCompileException e) {
            LogTool.traceError(ErrorType.HOOK_ERROR,
                    "insert before method " + method.getLongName() + " failed: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * (none-javadoc)
     *
     * @see AbstractClassHook#insertAfter(CtClass, String, String, String, boolean)
     */
    public void insertAfter(CtClass invokeClass, String methodName, String desc, String src)
            throws NotFoundException, CannotCompileException {
        insertAfter(invokeClass, methodName, desc, src, false);
    }

    /**
     * 在目标类的目标方法的出口插入相应的源代码
     *
     * @param method    目标方法
     * @param src       源代码
     * @param asFinally 是否在抛出异常的时候同样执行该源代码
     */
    public void insertAfter(CtBehavior method, String src, boolean asFinally) throws CannotCompileException {
        try {
            method.insertAfter(src, asFinally);
            LOGGER.info("insert after method: " + method.getLongName());
        } catch (CannotCompileException e) {
            LogTool.traceError(ErrorType.HOOK_ERROR,
                    "insert after method " + method.getLongName() + " failed: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取调用静态方法的代码字符串
     *
     * @param invokeClass 静态方法所属的类
     * @param methodName  静态方法名称
     * @param paramString 调用传入的参数字符串,按照javassist格式
     * @return 整合之后的代码
     */
    public String getInvokeStaticSrc(Class invokeClass, String methodName, String paramString, Class... parameterTypes) {
        String src;
        String invokeClassName = invokeClass.getName();

        String parameterTypesString = "";
        if (parameterTypes != null && parameterTypes.length > 0) {
            for (Class parameterType : parameterTypes) {
                if (parameterType.getName().startsWith("[")) {
                    parameterTypesString += "Class.forName(\"" + parameterType.getName() + "\"),";
                } else {
                    parameterTypesString += (parameterType.getName() + ".class,");
                }
            }
            parameterTypesString = parameterTypesString.substring(0, parameterTypesString.length() - 1);
        }
        if ("".equals(parameterTypesString)) {
            parameterTypesString = null;
        } else {
            parameterTypesString = "new Class[]{" + parameterTypesString + "}";
        }
        //默认都是当前的类加载器
        if (isLoadedByBootstrapLoader) {
            src = "com.endpoint.rasp.ModuleLoader.moduleClassLoader.loadClass(\"" + invokeClassName + "\").getMethod(\"" + methodName +
                    "\"," + parameterTypesString + ").invoke(null";
            if (paramString!=null&&!paramString.isEmpty()) {
                src += (",new Object[]{" + paramString + "});");
            } else {
                src += ",null);";
            }
            src = "try {System.out.print(1);" + src + "} catch (Throwable t) {if(t.getCause() != null && t.getCause().getClass()" +
                    ".getName().equals(\"com.endpoint.rasp.engine.common.exception.SecurityException\")){throw t;}}";
        } else {
            src = invokeClassName + '.' + methodName + "(" + paramString + ");";
            src = "try {System.out.print(1);" + src + "} catch (Throwable t) {if(t.getClass()" +
                    ".getName().equals(\"com.endpoint.rasp.engine.common.exception.SecurityException\")){throw t;}}";
        }
        return src;
    }
}
