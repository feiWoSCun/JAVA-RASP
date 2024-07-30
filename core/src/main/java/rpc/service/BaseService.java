package rpc.service;


import com.endpoint.rasp.common.constant.RaspArgsConstant;
import com.endpoint.rasp.engine.EngineBoot;
import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.CheckerManager;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;
import com.endpoint.rasp.engine.common.constant.RaspEngineConstant;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.common.LogTool;
import rpc.bean.RPCMemShellEventLog;
import rpc.bean.RaspConfig;
import rpc.enums.ServiceTypeEnum;

import static com.endpoint.rasp.engine.EngineBoot.args;

/**
 * 进程通信相关基础服务
 */
public abstract class BaseService {


    protected String ip;
    protected String port;
    protected static RaspConfig raspConfig = new RaspConfig();
    protected EngineBoot raspBootStrap;
    protected int warning_times = 0;

    /**
     * 适配原来的代码 默认使用rpc
     * 懒加载
     *
     * @return
     */
    public static BaseService getInstance() {

        return ServiceStrategyFactory.getInstance().getCommunicationStrategy(ServiceTypeEnum.MQ);
    }

    public static BaseService getInstance(ServiceTypeEnum serviceTypeEnum) {
        return ServiceStrategyFactory.getInstance().getCommunicationStrategy(serviceTypeEnum);
    }

    public BaseService(String port, String ip) {
        this.ip = ip;
        this.port = port;
    }


    public BaseService() {
        String ip = System.getProperty(RaspArgsConstant.IP);
        String port = System.getProperty(RaspArgsConstant.PORT);
        LogTool.info("get args from System.getProperty,ip:" + ip + ",port:" + port);
        //优先使用-D参数
        String ipD = args.get(RaspArgsConstant._IP);
        String portD = args.get(RaspArgsConstant._PORT);
        LogTool.info("get args from java -D,ip:" + ip + ",port:" + port);
        ip = ip == null ? ipD : ip;
        port = port == null ? portD : port;
        this.ip = ip == null ? RaspArgsConstant.SERVICE_IP : ip;
        this.port = port == null ? RaspArgsConstant.SERVICE_PORT : port;
    }

    protected void updateRaspConfig(RaspConfig config) {
        raspConfig.setId(config.getId());
        if (raspConfig.getRaspInfo() != null && raspConfig.getRaspInfo().getServerType() == null && config.getRaspInfo() != null && config.getRaspInfo().getServerType() != null) {
            raspConfig.getRaspInfo().setServerType(config.getRaspInfo().getServerType());
        }
        //开启关闭状态更新，需要同步变更引擎状态
        if (config.getRaspStatus() != null && !config.getRaspStatus().equals(raspConfig.getRaspStatus())) {
            updateRaspStatus(config);
        }
        //更新白名单列表
        raspConfig.updateMemShellWhiteConfig(config.getMemShellWhiteConfig());
        if (config.getProtectStatus() != null && !config.getProtectStatus().equals(raspConfig.getProtectStatus())) {
            updateBlockStatus(config);
        }
    }

    protected void updateBlockStatus(RaspConfig config) {

        //更新防护状态，并修改检测引擎的开关
        raspConfig.setProtectStatus(config.getProtectStatus());
        if (MemoryShellConstant.ACTION_WARNING.equals(raspConfig.getProtectStatus())) {
            CheckerManager.updateBlockStatus(CheckParameter.Type.MEMORYSHELL, false);
            raspConfig.getRaspInfo().setProtectStatus(MemoryShellConstant.ACTION_WARNING);
            LogTool.info("RASP 引擎修改为仅检测模式");
        } else if (MemoryShellConstant.ACTION_BLOCK.equals(raspConfig.getProtectStatus())) {
            CheckerManager.updateBlockStatus(CheckParameter.Type.MEMORYSHELL, true);
            raspConfig.getRaspInfo().setProtectStatus(MemoryShellConstant.ACTION_BLOCK);
            LogTool.info("RASP 引擎修改为阻断模式");
        } else {
            LogTool.error(ErrorType.UPDATE_DATA_ERROR, "Rasp Protect Status 数据异常：" + raspConfig.getProtectStatus());
        }
    }

    protected void updateRaspStatus(RaspConfig config) {
        raspConfig.setRaspStatus(config.getRaspStatus());
        if (RaspEngineConstant.RASP_ENGINE_STATUS_OPEN.equals(raspConfig.getRaspStatus())) {
            raspBootStrap.start();
            raspConfig.getRaspInfo().setStatus(RaspEngineConstant.RASP_ENGINE_STATUS_OPEN);
            LogTool.info("RASP 引擎状态修改为启动");
        } else if (RaspEngineConstant.RASP_ENGINE_STATUS_CLOSE.equals(raspConfig.getRaspStatus())) {
            raspBootStrap.stop();
            raspConfig.getRaspInfo().setStatus(RaspEngineConstant.RASP_ENGINE_STATUS_CLOSE);
            LogTool.info("RASP 引擎状态修改为关闭");
        } else {
            LogTool.error(ErrorType.UPDATE_DATA_ERROR, "Rasp Status 数据异常：" + raspConfig.getRaspStatus());
        }
    }

    public static void main(String[] args) {
        //baseService.init(null,System.getProperty("user.dir")+ File.separator+"lib"+File.separator);
    }


    /**
     * init
     */
    public abstract void init(EngineBoot boot, String libpath) throws InterruptedException;

    /**
     * 连接
     */
    public abstract void login();

    /**
     * 更新Rasp配置
     */
    public abstract void updateRaspConfig();

    /**
     * 发送Rasp告警
     */
    public abstract void sendRaspEventLog(RPCMemShellEventLog memShellEventLog);


    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    /**
     * 兼容rpc
     */
    public abstract String getRpcChannelHash();

    /**
     * 获取当前RASP引擎配置
     *
     * @return
     */
    public RaspConfig getRaspConfig() {
        return raspConfig;
    }

    public void close() {
    }

    protected void initIpAndAdder() {
    }


}
