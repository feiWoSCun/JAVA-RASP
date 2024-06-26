package com.endpoint.rasp.engine;

import com.endpoint.rasp.common.AnsiLog;
import com.endpoint.rasp.engine.checker.CheckerManager;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.endpoint.rasp.engine.transformer.CustomClassTransformer;
import org.apache.log4j.PropertyConfigurator;
import rpc.service.BaseService;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description:
 */
public class EngineBoot {
    public static final String ACTION = "action";
    public static final String UNINSTALL = "uninstall";
    public static final String INSTALL = "install";
    public static final String HOME = "home";
    public static final String LIB = "lib";
    private CustomClassTransformer transformer;
    private final Instrumentation instrumentation;
    /**
     * 注入进程PID
     */
    public static String raspPid;
    private final AtomicBoolean isBindRef = new AtomicBoolean(false);
    public static EngineBoot INSTANCE;
    public static String raspServerType = null;
    public static int VERSION = 1;
    public static Map<String, String> args;

    /**
     * @param inst tools.jar工具
     * @param args 参数
     * @return 单例
     * @throws Exception
     */
    public static synchronized EngineBoot getInstance(Instrumentation inst, Map<String, String> args) throws Exception {
        EngineBoot.args = args;
        //用户连续多次卸载
        String action = EngineBoot.args.get(ACTION);

        if (UNINSTALL.equals(action) && INSTANCE == null) {
            return null;
        }
        //用户连续多次安装
        if (INSTALL.equals(action) && INSTANCE != null) {
            return INSTANCE;
        }
        if (INSTANCE == null) {
            INSTANCE = new EngineBoot(inst, action);
            AnsiLog.info(AnsiLog.red("EngineBoot instance created，the classloader is：" + INSTANCE.getClass().getClassLoader()));
        }
        //执行卸载
        if (UNINSTALL.equals(action)) {
            try {
                INSTANCE.release();
            } catch (Exception e) {
                LogTool.info("EngineBoot release failed: " + e.getMessage());
                throw new RuntimeException("EngineBoot release failed EngineBoot#getInstance: " + e.getMessage());
            }
        }
        return INSTANCE;
    }


    /**
     * @param inst
     * @throws Exception
     */
    public EngineBoot(Instrumentation inst, String action) throws Exception {
        AnsiLog.info("load agent success,EngineBoot: it`s classloader :" + EngineBoot.class.getClassLoader());
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
        initIpAndPort();
        BaseService.getInstance().init(this, args.get(HOME) + File.separator + LIB + File.separator);

        AnsiLog.info("[E-RASP] Engine Initialized ");
    }

    private void initIpAndPort() {

        String ipAdder = System.getProperty("ip");
        String pt = System.getProperty("port");
        LogTool.info("get args from System.getProperty,ip:" + ipAdder + ",port:" + pt);
        Optional.ofNullable(ipAdder).ifPresent(t -> BaseService.ip = t);
        Optional.ofNullable(pt).ifPresent(t -> BaseService.port = t);
        //优先使用-D参数
        String ip = args.get("-ip");
        String port = args.get("-port");
        LogTool.info("get args from java -D,ip:" + ipAdder + ",port:" + pt);
        Optional.ofNullable(ip).ifPresent(t -> BaseService.ip = t);
        Optional.ofNullable(port).ifPresent(t -> BaseService.port = t);

    }

    public void release() {
// CpuMonitorManager.release();//停止CPU资源监控
        BaseService.getInstance().close();
        if (transformer != null) {
            transformer.release();
        }
        INSTANCE = null;
        //清除所有检测引擎
        CheckerManager.release();

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
