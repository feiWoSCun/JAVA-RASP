package com.endpoint.rasp.boot;


import com.endpoint.rasp.common.AnsiLog;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description:
 */
public class Bootstrap {
    private static final File RASP_HOME_DIR;
    private static final String JAR = "-jar";
    private static final String CORE = "-core";
    public static final String AGENT = "-agent";
    private static final String CORE_NAME = "rasp-core-shade.jar";
    private static final String AGENT_JAR = "agent.jar";

    static {
        CodeSource codeSource = Bootstrap.class.getProtectionDomain().getCodeSource();
        File bootJarPath;
        try {
            bootJarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
            RASP_HOME_DIR = bootJarPath.getParentFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        //debug用
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Bootstrap().startBoot(args);
    }

    private void startBoot(String[] args) {
        this.checkArgs(args);
        final List<String> command = new ArrayList<>();
        //组装java -jar path/core.jar
        final String corePath = new File(RASP_HOME_DIR, CORE_NAME).getAbsolutePath();
        System.out.println(corePath);
        setCommand(command, corePath);
        ProcessUtils.startArthasCore(args[0], command);
    }

    private void checkArgs(String[] args) {
        if (args == null || args.length == 0 || args[0] == null || args[0].isEmpty() || !args[0].matches("-?\\d+")) {
            AnsiLog.error("Please specify the arguments to bootstrap,we need a pid about target java process");
            AnsiLog.error("for example: java -jar boot.jar 12345,12345 is the target java process");
            System.exit(0);
        }
    }

    private void setCommand(List<String> command, String corePath) {
        command.add(JAR);
        command.add(corePath);
        command.add(CORE);
        command.add(corePath);
        command.add(AGENT);
        command.add(new File(RASP_HOME_DIR, AGENT_JAR).getAbsolutePath());
    }


}
