package com.endpoint.rasp.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description: 为什么不使用maven的dependency呢=》想做一个纯净的agent启动类
 */
public class AgentBootstrap {
    private static final String RASP_CORE_JAR = "core.jar";
    private static final String RASP_BOOTSTRAP = "com.endpoint.rasp.engine.RaspBootstrap";
    private static final String GET_INSTANCE = "getInstance";
    private static final String IS_BIND = "isBind";
    public static final String HOME = "-home";
    public static final String RASP_CORE_SHADE_JAR = "/rasp-core-shade.jar";
    public static final String AGENT_JAR = "/agent.jar";
    public static final String ACTION = "-action";
    public static final String INSTALL = "install";
    public static final String PID = "-pid";
    public static final String UNINSTALL = "uninstall";
    private static PrintStream ps = System.err;
    private static volatile ClassLoader raspClassLoader;
    private static Map<String, String> useArgs;

    public static final String LOGS_RASP_AGENT_LOG = "logs/rasp-agent.log";

    //agent日志文件 把PrintStream重定向到rasp-agent.log
    static {
        try {
            CodeSource codeSource = AgentBootstrap.class.getProtectionDomain().getCodeSource();
            File agentJarPath;
            agentJarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
            String raspLogDir = agentJarPath.getParentFile().getAbsolutePath();
            File logFile = new File(raspLogDir, LOGS_RASP_AGENT_LOG);
            File logDir = logFile.getParentFile();
            if (!logDir.exists()) {
                if (!logDir.mkdirs()) {
                    throw new RuntimeException("make log directory failed:");
                }
            }
            if (!logFile.createNewFile()) {
                throw new RuntimeException("create log file failed:");
            }
            ps = new PrintStream(new FileOutputStream(logFile, true));
        } catch (Throwable t) {
            //创建日志文件，失败也不退出
            t.printStackTrace(ps);
        }
    }

    /**
     * <pre>
     * 1. 全局持有classloader用于隔离 rasp 实现，防止多次attach重复初始化
     * 2. ClassLoader在rasp停止时会被reset
     * 3. 如果ClassLoader一直没变，则 RaspBootstrap#getInstance 返回结果一直是一样的
     * </pre>
     */


    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    private static synchronized void main(String args, final Instrumentation inst) {
        try {
            System.out.println("Starting Rasp Agent");
            ps.println("rasp server agent start...");
            args = args == null ? "" : args;
            args = decodeArg(args);

            useArgs = parseArgs(args);
            File raspCoreJarFile = new File(useArgs.get(HOME) + RASP_CORE_SHADE_JAR);
            // 如果 rasp-core 的 jar 文件不存在
            if (!raspCoreJarFile.exists()) {
                // 打印错误信息，提示找不到指定的 rasp-core jar 文件
                ps.println("Can not find rasp-core jar file from args: " + raspCoreJarFile);

                // 尝试从 rasp-agent.jar 所在的目录查找
                CodeSource codeSource = AgentBootstrap.class.getProtectionDomain().getCodeSource();

                // 如果 codeSource 不为空
                if (codeSource != null) {
                    try {
                        // 获取 rasp-agent.jar 文件的路径
                        File raspAgentJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());

                        // 在 rasp-agent.jar 所在的目录中查找 rasp-core.jar
                        raspCoreJarFile = new File(raspAgentJarFile.getParentFile(), RASP_CORE_JAR);

                        // 如果在 rasp-agent.jar 目录中也找不到 rasp-core.jar
                        if (!raspCoreJarFile.exists()) {
                            ps.println("Can not find rasp-core jar file from agent jar directory: " + raspAgentJarFile);
                        }
                    } catch (Throwable e) {
                        // 如果在尝试查找 rasp-core.jar 时发生异常，打印错误信息
                        ps.println("Can not find rasp-core jar file from " + codeSource.getLocation());
                        e.printStackTrace(ps);
                    }
                }
            }

            // 如果仍然找不到 rasp-core.jar 文件，则返回
            if (!raspCoreJarFile.exists()) {
                return;
            }

            final ClassLoader agentLoader = getClassLoader(inst, raspCoreJarFile);

            inst.appendToBootstrapClassLoaderSearch(new JarFile(useArgs.get(HOME) + RASP_CORE_SHADE_JAR));
            inst.appendToBootstrapClassLoaderSearch(new JarFile(useArgs.get(HOME) + AGENT_JAR));
            Thread bindingThread = new Thread(() -> {
                try {
                    bind(inst, agentLoader, useArgs);
                } catch (Throwable throwable) {
                    throwable.printStackTrace(ps);
                }
            });

            bindingThread.setName("rasp-binding-thread");
            bindingThread.start();
            bindingThread.join();
        } catch (Throwable t) {
            t.printStackTrace(ps);
            try {
                if (ps != System.err) {
                    ps.close();
                }
            } catch (Throwable tt) {
                // ignore
            }
            throw new RuntimeException(t);
        }
    }

    private static ClassLoader getClassLoader(Instrumentation inst, File raspCoreJarFile) throws Throwable {
        // 构造自定义的类加载器，尽量减少rasp对现有工程的侵蚀
        return loadOrDefineClassLoader(raspCoreJarFile);
    }

    private static ClassLoader loadOrDefineClassLoader(File raspCoreJarFile) throws Throwable {
        if (raspClassLoader == null) {
            raspClassLoader = new RaspClassloader(new URL[]{raspCoreJarFile.toURI().toURL()});
        }
        return raspClassLoader;
    }

    private static Map<String, String> parseArgs(String args) {

        final Map<String, String> map = new HashMap<>(4);
        final String[] split = args.split(";");


        for (int i = 0; i < split.length; ) {
            final int finalI = i;
            Optional.ofNullable(split[i]).ifPresent(k -> {
                assert split[finalI + 1] != null;
                identifyArg(split[finalI + 1], map, k);
            });
            i += 2;
        }

        return map;
    }

    private static void identifyArg(String v, Map<String, String> map, String k) {
        if (v.isEmpty()) {
            ps.println("parse null value! AgentBootStrap#parseArgs");
            System.exit(1);
        }
        map.put(k, v);
    }


    private static void initLogPath(File home, String pid) {
        System.setProperty("log-path", home + File.separator + "logs" + File.separator + "rasp" + pid + ".log");

    }

    private static void bind(Instrumentation inst, ClassLoader agentLoader, Map<String, String> useArgs) throws Throwable {
        /**
         * <pre>
         * raspBootstrap bootstrap = raspBootstrap.getInstance(inst);
         * </pre>
         */
        File home = new File(useArgs.get(HOME));
        initLogPath(home, useArgs.get(PID));
        //解决log4j  在不同线程初始化的问题
        ps.println("change Thread.currentThread.ContextClassLoader ,use RaspClassLoader");
        Thread.currentThread().setContextClassLoader(agentLoader);
        Class<?> bootstrapClass = agentLoader.loadClass(RASP_BOOTSTRAP);
        String action = useArgs.get(ACTION);
        Object bootstrap = bootstrapClass.getMethod(GET_INSTANCE, Instrumentation.class, Map.class).invoke(null, inst, useArgs);
        if (INSTALL.equals(action)) {
            boolean isBind = (Boolean) bootstrapClass.getMethod(IS_BIND).invoke(bootstrap);
            if (!isBind) {
                String errorMsg = "rasp server port binding failed! Please check $HOME/logs/rasp/rasp-agent.log for more details." + " AgentBootStrap#bind";
                ps.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            ps.println("rasp server already bind. AgentBootStrap#bind");
        } else if (UNINSTALL.equals(action)) {
            if (bootstrap != null) {
                String errorMsg = "rasp server unload failed! Please check logs/rasp/rasp-pid.log for more details." + " AgentBootStrap#bind";
                ps.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            ps.println("rasp server already unload. AgentBootStrap#bind");
        } else {
            throw new RuntimeException("unknown action: " + action);
        }

    }

    private static String decodeArg(String arg) {
        try {
            return URLDecoder.decode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }
}
