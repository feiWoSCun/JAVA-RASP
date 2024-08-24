package com.endpoint.rasp.engine.transformer;

import com.endpoint.rasp.CheckerContext;
import com.endpoint.rasp.Rule;
import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;
import com.endpoint.rasp.engine.EngineBoot;
import com.endpoint.rasp.engine.hook.GenerateContextHook;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义类字节码转换器，用于hook类的方法
 * 问：一个Java进程可以有几个ClassFileTransformer？
 * <p>
 * Created by yunchao.zheng on 2023-03-14
 */
public class CustomClassTransformer implements ClassFileTransformer {
    public static final Logger LOGGER = Logger.getLogger(CustomClassTransformer.class.getName());

    //true 已经卸载，false已经安装
    private volatile boolean loadFlag = true;
    private final Instrumentation inst;

    private List<Rule> recombinationCheckContainer;
    private Set<String> ruleOfClassNames;

    public boolean getLoadFlag() {
        return loadFlag;
    }


    public CustomClassTransformer(Instrumentation inst) {
        this.inst = inst;
        loadFlag = true;
        inst.addTransformer(this, true);
        getRules();
        getRuleOfClassNames();
    }

    private void getRuleOfClassNames() {
        this.ruleOfClassNames = this.recombinationCheckContainer.stream().map(Rule::getClassName).collect(Collectors.toSet());

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
        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        for (Class<?> clazz : loadedClasses) {
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
    private void getRules() {
        List<Rule> checkRuleContainer = CheckerContext.getCheckRuleContainer();

        Map<String, Rule> ruleMap = checkRuleContainer.stream().distinct().collect(Collectors.toMap(Rule::getKey, rule -> rule, (rule1, rule2) -> {
            final TreeSet<Integer> integers = new TreeSet<>();
            final Rule rule = new Rule();
            for (int argsIndex : rule1.getArgsIndex()) {
                integers.add(argsIndex);
            }
            for (int argsIndex : rule2.getArgsIndex()) {
                integers.add(argsIndex);
            }
            rule.setBit(rule1.getBit());
            rule.setArgsIndex(Arrays.stream(integers.toArray(new Integer[0])).mapToInt(Integer::intValue).toArray());
            rule.setClassName(rule1.getClassName());
            rule.setPattern(null);
            rule.setIfStatic(rule1.isIfStatic());
            rule.setMethodName(rule1.getMethodName());
            return rule;
        }));
        this.recombinationCheckContainer = new ArrayList<>(ruleMap.values());
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

            download(classfileBuffer, "/unload", "Main.class", className);
            return classfileBuffer;
        }

        //后面的判断是做测试用
        if (className.contains("rasp") && !className.contains("com/endpoint/rasp/Main")) {
            return classfileBuffer;
        }
        for (final Rule rule : recombinationCheckContainer) {
            if (className.equals(rule.getClassName())) {
                    download(classfileBuffer, "/before-load", "Main.class", className);

                CtClass ctClass = null;
                try {
                    ClassPool classPool = new ClassPool();
                    addLoader(classPool, loader);
                    ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                    classfileBuffer = GenerateContextHook.doHook(ctClass, rule.getKey(), "defaultCheckEnter", rule.getMethodName(),
                            rule.getArgsIndex(), rule.getDesc(), rule.isIfStatic());
                    download(classfileBuffer, "/after-load", "Main.class", className);
                } catch (IOException ignored) {

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

    /**
     * debug用，用来检测是否完成了字节码插桩
     *
     * @param bytes
     * @param dir
     * @param filePath
     * @param name
     */
    public void download(byte[] bytes, String dir, String filePath, String name) {


        try {
            CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();
            File path;
            path = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());

            path = new File(path.getParentFile().getAbsolutePath() + dir);
            boolean mkdirs = path.mkdirs();
            path = new File(path, filePath);
            boolean newFile = path.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(path)) {

                fos.write(bytes);
                LogTool.info("Byte array has been written to the file: " + filePath);
            } catch (IOException e) {
                System.err.println("An error occurred while writing the byte array to the file.");
                LogTool.error(ErrorType.PLUGIN_ERROR, "下载失败", e);

            }
        } catch (URISyntaxException | IOException e) {
            System.err.println("An error occurred while creating the directory.");
            LogTool.error(ErrorType.PLUGIN_ERROR, "下载失败", e);
            return; // 如果目录创建失败，退出程序
        }
    }

    /**
     * 是否是需要HOOK的类
     *
     * @param className
     * @return
     */
    public boolean isClassMatched(String className) {
        return ruleOfClassNames.contains(className);
    }

    private void addLoader(ClassPool classPool, ClassLoader loader) {
        classPool.appendSystemPath();
        classPool.appendClassPath(new ClassClassPath(EngineBoot.class));
        if (loader != null) {
            classPool.appendClassPath(new LoaderClassPath(loader));
        }
    }


}
