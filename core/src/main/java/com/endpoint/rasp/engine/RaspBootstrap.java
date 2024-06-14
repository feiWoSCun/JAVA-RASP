package com.endpoint.rasp.engine;

import com.endpoint.rasp.common.AnsiLog;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.endpoint.rasp.engine.transformer.CustomClassTransformer;
import org.apache.log4j.PropertyConfigurator;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean isBindRef = new AtomicBoolean(false);
    public static RaspBootstrap INSTANCE;
    public static String raspServerType = null;
    public static int VERSION = 1;

    /**
     * @param inst   tools.jar工具
     * @param action 安装或者卸载
     * @return 单例
     * @throws Exception
     */
    public static synchronized RaspBootstrap getInstance(Instrumentation inst, String action) throws Exception {
        //执行卸载
        if ("uninstall".equals(action)) {
            try {
                INSTANCE.release();
            } catch (Exception e) {
                LogTool.info("RaspBootstrap release failed: " + e.getMessage());
                throw new RuntimeException("RaspBootstrap release failed RaspBootstrap#getInstance: " + e.getMessage());
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new RaspBootstrap(inst, action);
        }
        AnsiLog.info(AnsiLog.red("RaspBootstrap instance created，the classloader is：" + INSTANCE.getClass().getClassLoader()));
        return INSTANCE;
    }


    /**
     * @param inst
     * @throws Exception
     */
    public RaspBootstrap(Instrumentation inst, String action) throws Exception {
        AnsiLog.info("load agent success,RaspBootstrap: it`s classloader :" + RaspBootstrap.class.getClassLoader());
        this.instrumentation = inst;
        PropertyConfigurator.configure(this.getClass().getResourceAsStream("/log4j.properties"));
        if (!loadConfig()) {
            return;
        }
        if (!isBindRef.compareAndSet(false, true)) {
            AnsiLog.warn("rasp already bind,plz check if you are rebinding");
            throw new IllegalStateException("rasp already bind,plz check if you are rebinding");
        }
        //与Agent通信成功后再修改字节码
        initTransformer();

        //测试代码：验证字节码是否被重新生成
/*        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                transformer.testRetransformHook();
            }
        }).start();*/
        //RPC服务初始化，连接EDRAgent,并创建心跳
        //TODO 单机测试关闭Agent通信
        //BaseService.getInstance().init(this, baseDir + File.separator + "lib" + File.separator);

        AnsiLog.info("[E-RASP] Engine Initialized ");
    }

    public void release() {
//        CpuMonitorManager.release();//停止CPU资源监控
        //BaseService.getInstance().close();
        if (transformer != null) {
            transformer.release();
        }
        //清除所有检测引擎
        //   CheckerManager.release();

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
        release();
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

    /**
     * 判断服务端是否已经启动
     *
     * @return true:服务端已经启动;false:服务端关闭
     */
    public boolean isBind() {
        return isBindRef.get();
    }
}
