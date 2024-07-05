package rpc.service;

import com.endpoint.rasp.engine.EngineBoot;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import rpc.enums.MqEnum;
import rpc.bean.RPCMemShellEventLog;
import rpc.bean.RaspConfig;
import rpc.bean.RaspInfo;
import rpc.enums.ServiceTypeEnum;
import rpc.job.SendRaspEventLogJob;
import rpc.job.ThreadPool;
import rpc.job.UpdateRaspConfigJob;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: feiwoscun
 * @date: 2024/6/14
 * @email: 2825097536@qq.com
 * @description:
 */
public class ZeroMQService extends BaseService implements ServiceStrategyHandler {
    public static final int RECONNECT_IVL = 2000;
    public static final int RECONNECT_IVL_MAX = 10000;
    private ZMQ.Socket socket;
    private ZMQ.Context context;
    private static final AtomicBoolean flag = new AtomicBoolean(false);


    public void init(EngineBoot boot) {
        super.initIpAndAdder();
        login();
        raspBootStrap = boot;
        RaspInfo raspInfo = new RaspInfo(EngineBoot.raspPid, EngineBoot.raspServerType, EngineBoot.VERSION);
        raspConfig.setRaspInfo(raspInfo);
        ThreadPool.exec(new UpdateRaspConfigJob());
        ThreadPool.exec(new SendRaspEventLogJob());
    }

    public void testMessage() {
        String s = this.sendAndGet(MqEnum.UPLOAD_LOG);
        LogTool.info(s);
        System.out.println(s);
    }


    @Override
    public void close() {
        this.socket.close();
        this.context.term();
    }

    public String sendAndGet(String send) {
        socket.send(send.getBytes());
        byte[] receive = socket.recv();
        String s = new String(receive);
        LogTool.info("[zeromq]:收到数据：" + s);
        return s;
    }

    public String sendAndGet(Object send) {
        String json = new Gson().toJson(send);
        socket.send(json.getBytes());
        byte[] receive = socket.recv();
        return new String(receive);
    }


    @Override
    public void init(EngineBoot boot, String libPath) {
        this.init(boot);
    }

    @Override
    public void login() {
        context = ZMQ.context(1);
        socket = context.socket(SocketType.REQ);

        LogTool.info("trying to Connect to server...,ip:" + ip + "," + "port:" + port);
        boolean connect = false;
        if (socket != null) {
            connect = socket.connect("tcp://" + ip + ":" + port);

        }
        if (connect) {
/*            socket.setReconnectIVL(RECONNECT_IVL);
            socket.setReconnectIVLMax(RECONNECT_IVL_MAX);*/
        } else {
            LogTool.error(ErrorType.REGISTER_ERROR, "false to connect to ZeroMQService server,ip=" + ip + ", port=" + port);
        }
    }

    @Override
    public String getRpcChannelHash() {
        return "zeromq";
    }

    @Override
    public void updateRaspConfig() {
        RaspConfig configRequest = new RaspConfig();
        configRequest.setId(raspConfig.getId());
        configRequest.setRaspInfo(raspConfig.getRaspInfo());
        configRequest.setCommand(MqEnum.UPDATE_RASP_INFO.getVal());
        String raspConfigStr = new Gson().toJson(configRequest);
        LogTool.info("rasp配置文件信息：" + raspConfigStr);
        try {
            String val = ThreadPool.submitAndGet(() -> sendAndGet(raspConfigStr));
            RaspConfig config = new Gson().fromJson(val, RaspConfig.class);
            LogTool.info("获取到配置文件信息：" + raspConfigStr);
            if (config != null && config.getRaspInfo() != null && config.getRaspInfo().getPid() != null && config.getRaspInfo().getPid().equals(raspConfig.getRaspInfo().getPid())) {
                if (config.getId() != null && !config.getId().equals(raspConfig.getId())) {
                    updateRaspConfig(config);
                }
            } else {
                LogTool.error(ErrorType.UPDATE_DATA_ERROR, "MQ 心跳数据异常：" + val);
            }
            LogTool.debug("call_update_rasp_info response: " + val);
        } catch (Exception e) {
            LogTool.error(ErrorType.UPDATE_DATA_ERROR, "MQ 心跳数据异常" + e.getMessage());
        }
    }

    @Override
    public void sendRaspEventLog(RPCMemShellEventLog memShellEventLog) {
        memShellEventLog.setCommand(MqEnum.UPLOAD_LOG.getVal());
        memShellEventLog.setRaspInfo(raspConfig.getRaspInfo());
        String eventStr = new Gson().toJson(memShellEventLog);
        LogTool.info("send log msg：" + eventStr);
        try {
            String response = ThreadPool.submitAndGet(() -> sendAndGet(new Gson().toJson(memShellEventLog)));
            LogTool.info("call_upload_rasp_log response: " + response);
        } catch (Exception e) {
            LogTool.error(ErrorType.UPDATE_DATA_ERROR, "sendRaspEventLog error at ZeroMQService#sendRaspEventLog", e);
        }
    }

    @Override
    public ServiceTypeEnum getServiceType() {
        return ServiceTypeEnum.MQ;
    }
}