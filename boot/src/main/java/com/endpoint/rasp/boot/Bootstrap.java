package com.endpoint.rasp.boot;


import com.endpoint.rasp.common.AnsiLog;
import com.endpoint.rasp.common.constant.RaspArgsConstant;
import org.apache.commons.cli.*;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description:
 */
public class Bootstrap {
    private static File RASP_HOME_DIR;
    private static String pid = "-1";
    private static String ip = "127.0.0.1";
    private static String port = "10574";
    private String install;

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

        setCommand(command, install);
        ProcessUtils.startRaspCore(command);
    }

    /**
     * 解析java -jar的参数  直接用魔术值
     *
     * @param args
     */
    private void parseArgs(String[] args) {
        try {
            Options options = new Options();
            options.addOption("pid", true, "Specify the pid of Java server to attach");
            options.addOption("install", true, "execute install，specify the path of e-rasp");
            options.addOption("uninstall", true, "execute uninstall，specify the path of e-rasp");
/*            options.addOption("ip", true, "Specify the ip of Java server to attach");
            options.addOption("port", true, "Specify the port of Java server to attach");*/
            //解析命令调用
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            HelpFormatter helpFormatter = new HelpFormatter();
            if (cmd.hasOption("help")) {
                helpFormatter.printHelp("java -jar boot.jar", options, true);
            }
            //获取安装或卸载RASP引擎（jar）的路径
            if (cmd.hasOption("install")) {
                RASP_HOME_DIR = new File(cmd.getOptionValue("install"));
                install = "install";
            } else if (cmd.hasOption("uninstall")) {
                RASP_HOME_DIR = new File(cmd.getOptionValue("uninstall"));
                install = "uninstall";
            } else {
                AnsiLog.error("One of -install and -uninstall must be specified");
                returnWrongMsg();
            }
            if (cmd.hasOption("ip")) {
                ip = cmd.getOptionValue("ip");
            }
            if (cmd.hasOption("port")) {
                port = cmd.getOptionValue("port");
            }
            //获取进程ID
            if (cmd.hasOption("pid") && this.checkPid(pid)) {
                pid = cmd.getOptionValue("pid");
            } else {
                returnWrongMsg();
            }
            String pt = System.getProperty("port");
            AnsiLog.info("get args from System.getProperty,port:"+pt);
            Optional.ofNullable(pt).ifPresent(t ->port = t);
            String ipAdder = System.getProperty("ip");
            AnsiLog.info("get args from System.getProperty,ip:"+ipAdder);
            Optional.ofNullable(ipAdder).ifPresent(t ->ip = t);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean checkPid(String pid) {
        return pid != null && !pid.isEmpty() && pid.matches("-?\\d+");
    }

    private static void returnWrongMsg() {
        AnsiLog.error("Please specify the arguments to bootstrap,we need a pid about target java process\n,for example: java -jar boot.jar -pid 12345, -install home/f/...\n" +
                "12345 is the target java process;\n home/f/...is your boot.jar path");
        System.exit(1);
    }

    /**
     *
     * @param command
     * @param install
     */
    private void setCommand(List<String> command, String install) {
        final String corePath = new File(RASP_HOME_DIR, RaspArgsConstant.CORE_NAME).getAbsolutePath();
        command.add(RaspArgsConstant._JAR);
        command.add(corePath);
        command.add(RaspArgsConstant._HOME);
        command.add(RASP_HOME_DIR.getAbsolutePath());
        command.add(RaspArgsConstant._ACTION);
        command.add("install".equals(install) ? install : "uninstall");
        command.add(RaspArgsConstant._PID);
        command.add(pid);
        command.add(RaspArgsConstant._IP);
        command.add(ip);
        command.add(RaspArgsConstant._PORT);
        command.add(port);
    }


}
