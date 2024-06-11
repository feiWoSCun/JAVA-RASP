package com.endpoint.rasp.engine;

/**
 * @author: feiwoscun
 * @date: 2024/6/7
 * @email: 2825097536@qq.com
 * @description:
 */


import com.endpoint.rasp.common.AnsiLog;
import com.endpoint.rasp.common.JavaVersionUtils;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description:
 */
public class Core {
    //"java -xbootpath: /home....  -pid 2200 "
    private Core(String[] args) throws Exception {
        this.getConfig(args);
        CONFIG.printSelf();
        this.attachAgent();
    }

    private void doAttach() {

    }

    /**
     * a map to contain args;
     */
    private final static ArgsHashMap CONFIG = new ArgsHashMap(4);
    private final static Map<String, Boolean> DEFAULT_CONFIG;

    static {

        //k：参数 ，v：是否必须
        DEFAULT_CONFIG = new HashMap<>(4);
        DEFAULT_CONFIG.put("-pid", true);
        DEFAULT_CONFIG.put("-core", true);
        DEFAULT_CONFIG.put("-agent", true);
    }

    public static void main(String[] args) {
        try {
            new Core(args);
        } catch (Throwable t) {
            // AnsiLog.error("Start arthas failed, exception stack trace: ");
            t.printStackTrace();
            System.exit(-1);
        }
    }

    private void attachAgent() throws Exception {
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        final String tarPid = CONFIG.get("-pid");
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            String pid = descriptor.id();
            if (pid.equals(tarPid)) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }
        VirtualMachine virtualMachine = null;
        try {
            if (null == virtualMachineDescriptor) { // 使用 attach(String pid) 这种方式
                virtualMachine = VirtualMachine.attach("" + tarPid);
            } else {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
            }

            Properties targetSystemProperties = virtualMachine.getSystemProperties();
            String targetJavaVersion = JavaVersionUtils.javaVersionStr(targetSystemProperties);
            String currentJavaVersion = JavaVersionUtils.javaVersionStr();
            if (targetJavaVersion != null && currentJavaVersion != null) {
                if (!targetJavaVersion.equals(currentJavaVersion)) {
                    AnsiLog.warn("Current VM java version: {} do not match target VM java version: {}, attach may fail.",
                            currentJavaVersion, targetJavaVersion);
                    AnsiLog.warn("Target VM JAVA_HOME is {}, arthas-boot JAVA_HOME is {}, try to set the same JAVA_HOME.",
                            targetSystemProperties.getProperty("java.home"), System.getProperty("java.home"));
                }
            }
            System.out.println("feiwoscun:"+CONFIG.getAgentPath());
            //CONFIG.setAgentPath();
            //CONFIG.setCorePath();
            try {
                virtualMachine.loadAgent(CONFIG.getAgentPath(),CONFIG.getCorePath() +";" + CONFIG.getAgentPath());
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("Non-numeric value found")) {
                    AnsiLog.warn(e);
                    AnsiLog.warn("It seems to use the lower version of JDK to attach the higher version of JDK.");
                    AnsiLog.warn(
                            "This error message can be ignored, the attach may have been successful, and it will still try to connect.");
                } else {
                    throw e;
                }
            } catch (com.sun.tools.attach.AgentLoadException ex) {
                if ("0".equals(ex.getMessage())) {
                    // https://stackoverflow.com/a/54454418
                    AnsiLog.warn(ex);
                    AnsiLog.warn("It seems to use the higher version of JDK to attach the lower version of JDK.");
                    AnsiLog.warn(
                            "This error message can be ignored, the attach may have been successful, and it will still try to connect.");
                } else {
                    throw ex;
                }
            }
        } finally {
            if (null != virtualMachine) {
                virtualMachine.detach();
            }
        }
    }


    private void getConfig(String... args) {
        int index = 0;
        while (index < args.length) {
            String arg = args[index];
            if (DEFAULT_CONFIG.containsKey(arg)) {
                if (!DEFAULT_CONFIG.get(arg)) {
                    CONFIG.put(arg, null);
                } else {
                    CONFIG.put(arg, args[++index]);
                    DEFAULT_CONFIG.remove(arg);
                }
            } else {
                System.out.println(AnsiLog.red("unresolved arg: " + arg));
            }
            index++;
        }
        StringBuilder resolveMsg = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : DEFAULT_CONFIG.entrySet()) {
            resolveMsg.append(entry.getKey()).append(",");
        }
        if (resolveMsg.length() > 0) {
            resolveMsg.insert(0, "there are some args need like :");
            AnsiLog.error(resolveMsg.toString());
            System.exit(0);
        }

    }
}
