package com.endpoint.rasp.engine;

import com.endpoint.rasp.common.AnsiLog;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.endpoint.rasp.engine.transformer.CustomClassTransformer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * E-Rasp引擎入口类
 * <p>
 * Created by yunchao.zheng on 2023-03-14
 */
public class RaspBootstrap {
    public static final Logger LOGGER = Logger.getLogger(RaspBootstrap.class.getName());

    private CustomClassTransformer transformer;
    private final Instrumentation instrumentation;
    private final Logger logger = Logger.getLogger(RaspBootstrap.class.getName());
    /**
     * 注入进程PID
     */
    public static String raspPid;

    public static RaspBootstrap INSTANCE;
    public static String raspServerType = null;
    public static int VERSION=1;

    public static synchronized RaspBootstrap getInstance(Instrumentation inst, String args) throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new RaspBootstrap(inst, args);
        }
        AnsiLog.info(AnsiLog.red("RaspBootstrap instance created，the classloader is：" + INSTANCE.getClass().getClassLoader()));
        return INSTANCE;
    }



    /**
     * @param inst
     * @param agentArgs 目前只有“-agent /path”
     * @throws Exception
     */
    public RaspBootstrap(Instrumentation inst, String agentArgs) throws Exception {
        AnsiLog.info(AnsiLog.red("load agent success,RaspBootstrap: it`s classloader is :" + RaspBootstrap.class.getClassLoader()));
        String baseDir = new File(agentArgs).getParent();
        this.instrumentation = inst;
        AnsiLog.info(baseDir);
        //此时Log4j Appender的ClassLoader是Boot，如果agent中已经使用过，那Appender的ClassLoader会是App，会存在Appender无法找到，导致log4j初始化失败
        System.setProperty("log-path", baseDir + File.separator + "logs" + File.separator+"rasp.log");
        PropertyConfigurator.configure(RaspBootstrap.class.getResourceAsStream("/log4j.properties"));
        logger.info("[E-RASP] Engine Starting，PID{" + raspPid + "} ");
//        logger.debug("ProcessBuilderHook class loader:"+ ProcessBuilderHook.class.getClassLoader());
        if (!loadConfig()) {
            return;
        }
        //TODO 默认应该是关闭的。与Agent通信成功后再修改字节码
        initTransformer();
        //TODO 测试代码：验证字节码是否被重新生成
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                transformer.testRetransformHook();
            }
        }).start();
        //RPC服务初始化，连接EDRAgent,并创建心跳
        //TODO 单机测试关闭Agent通信
        //BaseService.getInstance().init(this, baseDir + File.separator + "lib" + File.separator);

        logger.info("[E-RASP] Engine Initialized ");
    }

    public void release(String mode) {
//        CpuMonitorManager.release();//停止CPU资源监控
        if (transformer != null) {
            transformer.release();
        }
        //清除所有检测引擎
//        CheckerManager.release();
        logger.info("[E-RASP] Engine Released ");
    }


    /**
     * 初始化配置
     *
     * @return 配置是否成功，与Agent通信是否正常
     */
    private boolean loadConfig() throws Exception {
//        LogConfig.ConfigFileAppender();
        //检查与引擎使用Agent的通信状况
        return true;
    }

    /**
     * 启动引擎
     */
    public void start() {
        LOGGER.debug("start engine");
        initTransformer();
    }

    /**
     * 关闭引擎,恢复字节码
     */
    public void stop() {
        LOGGER.debug("stop engine");
        release(null);
    }

    /**
     * 初始化类字节码的转换器
     */
    private void initTransformer() {
        transformer = new CustomClassTransformer(instrumentation);
        transformer.retransformHooks();
    }

    public void upgrade(Instrumentation inst) {
//        if (transformer != null) {
//            release(null);
//            transformer.retransformEngine();
//        }
////        transformer = new CustomClassTransformer(instrumentation);
////        transformer.retransformHooks();
//        instrumentation = inst;
//        try {
//            initTransformer(inst);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
////        deleteTmpDir();
//        //Test--测试定时更新
////        TestUpgrade.upgradeCycle(this);
    }
}