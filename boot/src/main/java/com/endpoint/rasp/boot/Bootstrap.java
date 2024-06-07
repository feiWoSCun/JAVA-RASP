package com.endpoint.rasp.boot;


import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {
    private static final String JAR = "-jar";
    private static final File RASP_HOME_DIR;
    private static final String CORE_NAME = "rasp-core-shade.jar";

    static {
        CodeSource codeSource = Bootstrap.class.getProtectionDomain().getCodeSource();

        File bootJarPath = null;
        try {
            bootJarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
            RASP_HOME_DIR = bootJarPath.getParentFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        final List<String> command = new ArrayList<>();
        //组装java -jar path/core.jar
        final String corePath = new File(RASP_HOME_DIR, CORE_NAME).getAbsolutePath();
        System.out.println(corePath);
        command.add(JAR);
        command.add(corePath);
        ProcessUtils.startArthasCore(Long.parseLong(args[0]), command);
    }


}
