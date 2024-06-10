package com.endpoint.rasp.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description:
 */
public class AgentBootstrap {
    private static final String RASP_CORE_JAR = "core.jar";
    private static final String RASP_BOOTSTRAP = "com.endpoint.rasp.engine.RaspBootstrap";
    private static final String GET_INSTANCE = "getInstance";
    private static final String IS_BIND = "isBind";

    private static PrintStream ps = System.err;
    static {
        try {
            File raspLogDir = new File(System.getProperty("user.home") + File.separator + "logs" + File.separator
                    + "rasp" + File.separator);
            if (!raspLogDir.exists()) {
                raspLogDir.mkdirs();
            }
            if (!raspLogDir.exists()) {
                // #572
                raspLogDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "logs" + File.separator
                        + "rasp" + File.separator);
                if (!raspLogDir.exists()) {
                    raspLogDir.mkdirs();
                }
            }

            File log = new File(raspLogDir, "rasp.log");

            if (!log.exists()) {
                log.createNewFile();
            }
            ps = new PrintStream(new FileOutputStream(log, true));
        } catch (Throwable t) {
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
    private static volatile ClassLoader raspClassLoader;

    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    /**
     * 让下次再次启动时有机会重新加载
     */
    public static void resetraspClassLoader() {
        raspClassLoader = null;
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

    private static synchronized void main(String args, final Instrumentation inst) {
        try {
            System.out.println("Starting Rasp Agent");
            ps.println("rasp server agent start...");
            // 传递的args参数分两个部分:raspCoreJar路径和agentArgs, 分别是Agent的JAR包路径和期望传递到服务端的参数
            if (args == null) {
                args = "";
            }
            args = decodeArg(args);

            String raspCoreJar;
            final String agentArgs;
            int index = args.indexOf(';');
            if (index != -1) {
                raspCoreJar = args.substring(0, index);
                agentArgs = args.substring(index);
            } else {
                raspCoreJar = "";
                agentArgs = args;
            }

            File raspCoreJarFile = new File(raspCoreJar);
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

            /**
             * Use a dedicated thread to run the binding logic to prevent possible memory leak. #195
             */
            final ClassLoader agentLoader = getClassLoader(inst, raspCoreJarFile);

            Thread bindingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        bind(inst, agentLoader, agentArgs);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace(ps);
                    }
                }
            };

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

    private static void bind(Instrumentation inst, ClassLoader agentLoader, String args) throws Throwable {
        /**
         * <pre>
         * raspBootstrap bootstrap = raspBootstrap.getInstance(inst);
         * </pre>
         */
        Class<?> bootstrapClass = agentLoader.loadClass(RASP_BOOTSTRAP);
        Object bootstrap = bootstrapClass.getMethod(GET_INSTANCE, Instrumentation.class, String.class).invoke(null, inst, args);
       /* boolean isBind = (Boolean) bootstrapClass.getMethod(IS_BIND).invoke(bootstrap);
        if (!isBind) {
            String errorMsg = "rasp server port binding failed! Please check $HOME/logs/rasp/rasp.log for more details.";
            ps.println(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        ps.println("rasp server already bind.");*/
    }

    private static String decodeArg(String arg) {
        try {
            return URLDecoder.decode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }
}
