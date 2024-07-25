package com.endpoint.rasp.engine.transformer;

import com.endpoint.rasp.engine.EngineBoot;
import com.endpoint.rasp.engine.common.annotation.AnnotationScanner;
import com.endpoint.rasp.engine.common.annotation.HookAnnotation;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.endpoint.rasp.engine.hook.AbstractClassHook;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义类字节码转换器，用于hook类的方法
 * 问：一个Java进程可以有几个ClassFileTransformer？
 * <p>
 * Created by yunchao.zheng on 2023-03-14
 */
public class CustomClassTransformer implements ClassFileTransformer {
    public static final Logger LOGGER = Logger.getLogger(CustomClassTransformer.class.getName());
    private static final String SCAN_ANNOTATION_PACKAGE = "com.endpoint.rasp.engine.hook";
    private static final HashSet<String> jspClassLoaderNames = new HashSet<>();
    public static ConcurrentHashMap<String, SoftReference<ClassLoader>> jspClassLoaderCache = new ConcurrentHashMap<String, SoftReference<ClassLoader>>();
    //true 已经卸载，false已经安装
    private volatile boolean loadFlag = true;
    private final Instrumentation inst;
    private final HashSet<AbstractClassHook> hooks = new HashSet<AbstractClassHook>();

    public boolean getLoadFlag() {
        return loadFlag;
    }

    static {
        jspClassLoaderNames.add("org.apache.jasper.servlet.JasperLoader");
        jspClassLoaderNames.add("com.caucho.loader.DynamicClassLoader");
        jspClassLoaderNames.add("com.ibm.ws.jsp.webcontainerext.JSPExtensionClassLoader");
        jspClassLoaderNames.add("weblogic.servlet.jsp.JspClassLoader");
        jspClassLoaderNames.add("com.tongweb.jasper.servlet.JasperLoader");
    }

    public CustomClassTransformer(Instrumentation inst) {
        this.inst = inst;
        loadFlag = true;
        inst.addTransformer(this, true);
        addAnnotationHook();
    }

    public void release() {

        if (!loadFlag) {
            LogTool.info("【rasp】卸载失败，可能rasp已经执行过卸载");
        }
        LOGGER.debug("【rasp】开始卸载rasp");
        loadFlag = false;
        //再次执行
        retransformHooks();
        //移除当前的transformer。重新加载的类将不会被应用这个transformer
        inst.removeTransformer(this);

    }

    /**
     * 获取当前所有被加载的类，修改需要Hook的类的字节码，并通知JVM重新加载被Hook的类
     */
    public void retransformHooks() {
        Class[] loadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : loadedClasses) {
            //是否匹配待HOOK的类
            if (isClassMatched(clazz.getName().replace(".", "/"))) {
                //确定类是否可被修改
                if (inst.isModifiableClass(clazz) && !clazz.getName().startsWith("java.lang.invoke.LambdaForm")) {
                    try {
                        LOGGER.debug("【rasp】重新加载类:" + clazz.getName());
                        // hook已经加载的类，或者是回滚已经加载的类(当转换器还原到默认值，就会执行类的还原)
                        inst.retransformClasses(clazz);
                    } catch (Throwable t) {
                        LogTool.error(ErrorType.HOOK_ERROR,
                                "failed to retransform class " + clazz.getName() + ": " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    /**
     * 测试当前已加载的类字节码
     */
    public void testRetransformHook() {
        Class[] loadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : loadedClasses) {
            //是否匹配待HOOK的类
            if (isClassMatched(clazz.getName().replace(".", "/"))) {
                //确定类是否可被修改
                if (inst.isModifiableClass(clazz) && !clazz.getName().startsWith("java.lang.invoke.LambdaForm")) {
                    try {
                        if ("io.undertow.servlet.handlers.ServletHandler".equals(clazz.getName())) {
//                            clazz.getConstructor().newInstance();
                            inst.retransformClasses(clazz);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        LogTool.error(ErrorType.HOOK_ERROR,
                                "failed to retransform class " + clazz.getName() + ": " + t.getMessage(), t);
                    }
                }
            }
        }
    }
    /**
     * 重新加载引擎类,用于升级引擎本身，暂时无效
     */
//    public void retransformEngine() {
//        Class[] loadedClasses = inst.getAllLoadedClasses();
//        for (Class clazz : loadedClasses) {
//            //是否为com.endpoint.rasp.engine包下面的类
//            if (clazz.getName().contains("engine")&&!clazz.getName().contains("CustomClassTransformer")&&!clazz.getName().contains("EngineBoot")&&!clazz.getName().contains("CheckParameter$Type")&&!clazz.getName().contains("ErrorType")&&!clazz.getName().contains("log4j")) {
//                try {
////                    inst.redefineClasses();
//                    // 刷新clazz
//                    inst.retransformClasses(clazz);
//                } catch (Throwable t) {
//                    LogTool.error(ErrorType.UPGRADE_ERROR,
//                            "failed to upgrade class " + clazz.getName() + ": " + t.getMessage(), t);
//                }
//            }
//        }
//    }

    /**
     * 将注解类名添加到HOOK集合中
     *
     * @param hook
     * @param className
     */
    private void addHook(AbstractClassHook hook, String className) {
        LOGGER.info("loading hook engine " + hook.getType() + " , hook-class name: " + className);
        hooks.add(hook);
    }

    /**
     * 扫描执行HOOK的注解类
     */
    private void addAnnotationHook() {
        Set<Class> classesSet = AnnotationScanner.getClassWithAnnotation(SCAN_ANNOTATION_PACKAGE, HookAnnotation.class);
        for (Class clazz : classesSet) {
            try {
                Object object = clazz.newInstance();
                if (object instanceof AbstractClassHook) {
                    addHook((AbstractClassHook) object, clazz.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogTool.error(ErrorType.HOOK_ERROR, "add hook failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 过滤需要hook的类，进行字节码更改
     *
     * @see ClassFileTransformer#transform(ClassLoader, String, Class, ProtectionDomain, byte[])
     */


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain domain, byte[] classfileBuffer) {

        if (!loadFlag) {
            LogTool.info(loader + "," + className);

            download(classfileBuffer, "/home/f/文档/java_project/e-rasp-renew/boot/target/unload", className);
            return classfileBuffer;
        }

        if (loader != null && jspClassLoaderNames.contains(loader.getClass().getName())) {
            jspClassLoaderCache.put(className.replace("/", "."), new SoftReference<>(loader));
        }
        if (className.contains("rasp")) {
            return classfileBuffer;
        }
        for (final AbstractClassHook hook : hooks) {
            if (hook.isClassMatched(className)) {

                if ("org/apache/catalina/core/StandardContext".equals(className)) {
                    download(classfileBuffer, "/home/f/文档/java_project/e-rasp-renew/boot/target/before-load", className);

                }
                //TODO 是否泄露原理
                LogTool.info("hook class name：" + className);
                //TODO 需关闭
                if ("io/undertow/servlet/handlers/ServletHandler".equals(className)) {
                    try {
                        System.out.println("hook before(file):\n\r" + new String(Base64.encode(classfileBuffer)));
                        InputStream iInputStream = loader.getResourceAsStream(className + ".class");
                        if (iInputStream == null) {
                            return classfileBuffer;
                        }
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        int nRead;
                        byte[] data = new byte[1024];
                        while ((nRead = iInputStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();
                        byte[] oldClassFileBytes = buffer.toByteArray();
                        System.out.println("hook before(in memory):\n\r" + new String(Base64.encode(oldClassFileBytes)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                CtClass ctClass = null;
                try {
                    ClassPool classPool = new ClassPool();
                    addLoader(classPool, loader);
                    ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                /*    if (loader == null) {
                        //没有指定构造器，则使用boot构造器
                        hook.setLoadedByBootstrapLoader(true);
                    }*/
                    hook.setLoadedByBootstrapLoader(true);
                    classfileBuffer = hook.transformClass(ctClass);
                    download(classfileBuffer, "/home/f/文档/java_project/e-rasp-renew/boot/target/after-load", className);
                    //TODO 需关闭
                    if ("io/undertow/servlet/handlers/ServletHandler".equals(className)) {
                        System.out.println("hook after:\n\r" + new String(Base64.encode(classfileBuffer)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (ctClass != null) {
                        ctClass.detach();
                    }
                }
            }
        }
//        serverDetector.detectServer(className, loader, domain);
        return classfileBuffer;
    }

    public void download(byte[] bytes, String filePath, String name) {


        if (!"org/apache/catalina/core/StandardContext".equals(name)) {
            return;

        }

        Path directoryPath = Paths.get(filePath);

        // 检查目录是否存在，不存在则创建
        try {
            if (directoryPath != null && !Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                System.out.println("Directory created: " + directoryPath);
            }
        } catch (IOException e) {
            System.err.println("An error occurred while creating the directory.");
            e.printStackTrace();
            return; // 如果目录创建失败，退出程序
        }
        // 将 byte 数组写入文件
        filePath += "/StandardContext.class";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {

            fos.write(bytes);
            System.out.println("Byte array has been written to the file: " + filePath);
        } catch (IOException e) {
            System.err.println("An error occurred while writing the byte array to the file.");
            e.printStackTrace();
        }
    }

    /**
     * 是否是需要HOOK的类
     *
     * @param className
     * @return
     */
    public boolean isClassMatched(String className) {
        for (final AbstractClassHook hook : getHooks()) {
            if (hook.isClassMatched(className)) {
                return true;
            }
        }
        return false;
//        return serverDetector.isClassMatched(className);
    }

    private void addLoader(ClassPool classPool, ClassLoader loader) {
        classPool.appendSystemPath();
        classPool.appendClassPath(new ClassClassPath(EngineBoot.class));
        if (loader != null) {
            classPool.appendClassPath(new LoaderClassPath(loader));
        }
    }

    public HashSet<AbstractClassHook> getHooks() {
        return hooks;
    }

}
