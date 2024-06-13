package com.endpoint.rasp.boot;


import com.endpoint.rasp.common.AnsiLog;
import org.apache.commons.cli.*;

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
    private static String pid = "-1";

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
        new Bootstrap().startBoot(args);
    }

    private void startBoot(String[] args) {
        this.parseArgs(args);
        final List<String> command = new ArrayList<>();
        //组装java -jar path/core.jar
        final String corePath = new File(RASP_HOME_DIR, CORE_NAME).getAbsolutePath();
        System.out.println(corePath);
        setCommand(command, corePath);
        ProcessUtils.startRaspCore(pid, command);
    }

    /**
     * 解析java -jar的参数  直接用魔术值
     * @param args
     */
    private void parseArgs(String[] args) {
        try {
            Options options = new Options();
            options.addOption("pid", true, "Specify the pid of Java server to attach");
            //解析命令调用
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            HelpFormatter helpFormatter = new HelpFormatter();
            if (cmd.hasOption("help")) {
                helpFormatter.printHelp("java -jar boot.jar", options, true);
            }
            //获取进程ID
            if ( cmd.hasOption("pid") && this.checkPid(pid)){
                pid = cmd.getOptionValue("pid");
            } else{
                returnWrongMsg();
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean checkPid(String pid) {
        return pid != null && !pid.isEmpty() && pid.matches("-?\\d+");
    }

    private static void returnWrongMsg() {
        AnsiLog.error("Please specify the arguments to bootstrap,we need a pid about target java process");
        AnsiLog.error("for example: java -jar boot.jar 12345,12345 is the target java process");
        System.exit(1);
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
