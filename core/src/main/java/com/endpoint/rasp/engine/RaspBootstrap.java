package com.endpoint.rasp.engine;

import com.endpoint.rasp.common.AnsiLog;
import com.endpoint.rasp.engine.transformer.CustomClassTransformer;
import org.apache.log4j.PropertyConfigurator;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.TimeUnit;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description:
 */
public class RaspBootstrap {
    private CustomClassTransformer transformer;
    private final Instrumentation instrumentation;
    /**
     * 注入进程PID
     */
    public static String raspPid;

    public static RaspBootstrap INSTANCE;
    public static String raspServerType = null;
    public static int VERSION=1;

    /**
     *
     * @param inst tools.jar工具
     * @param args 留着后续可能会有拓展
     * @return 单例
     * @throws Exception
     */
    public static synchronized RaspBootstrap getInstance(Instrumentation inst, String args) throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new RaspBootstrap(inst);
        }
        AnsiLog.info(AnsiLog.red("RaspBootstrap instance created，the classloader is：" + INSTANCE.getClass().getClassLoader()));
        return INSTANCE;
    }



    /**
     * @param inst
     * @throws Exception
     */
    public RaspBootstrap(Instrumentation inst) throws Exception {
        AnsiLog.info("load agent success,RaspBootstrap: it`s classloader :" + RaspBootstrap.class.getClassLoader());
        this.instrumentation = inst;
        PropertyConfigurator.configure(this.getClass().getResourceAsStream("/log4j.properties"));
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

        AnsiLog.info("[E-RASP] Engine Initialized ");
    }

    public void release(String mode) {
//        CpuMonitorManager.release();//停止CPU资源监控
        if (transformer != null) {
            transformer.release();
        }
        //清除所有检测引擎
//        CheckerManager.release();

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
        initTransformer();
    }

    /**
     * 关闭引擎,恢复字节码
     */
    public void stop() {
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
