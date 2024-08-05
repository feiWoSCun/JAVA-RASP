package rpc.service;


import com.endpoint.rasp.common.LogTool;
import com.endpoint.rasp.common.constant.RaspArgsConstant;
import com.endpoint.rasp.engine.EngineBoot;
import rpc.enums.ServiceTypeEnum;

import static com.endpoint.rasp.engine.EngineBoot.args;

/**
 * 进程通信相关基础服务
 */
public abstract class BaseService {


    protected String ip;
    protected String port;
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


    public static void main(String[] args) {
        //baseService.init(null,System.getProperty("user.dir")+ File.separator+"lib"+File.separator);
    }

    public String sendAndGet(String send) {
        return null;
    }

    /**
     * init
     */
    public abstract void init(EngineBoot boot, String libpath) throws InterruptedException;

    /**
     * 连接
     */
    public abstract void login();


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

    public void close() {
    }

    protected void initIpAndAdder() {
    }


}
