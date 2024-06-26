package rpc.service;

import com.endpoint.rasp.engine.EngineBoot;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import rpc.bean.RPCMemShellEventLog;
import rpc.bean.RaspConfig;
import rpc.bean.RaspInfo;
import rpc.job.SendRaspEventLogJob;
import rpc.job.UpdateRaspConfigJob;

/**
 * @author: feiwoscun
 * @date: 2024/6/14
 * @email: 2825097536@qq.com
 * @description:
 */
public class ZeroMQService extends BaseService {
    private ZMQ.Socket socket;
    private ZMQ.Context context;
    public static ZeroMQService INSTANCE = new ZeroMQService();

    static {
        beans.put("jeroMq", INSTANCE);
    }

    public void init(EngineBoot boot) {
        login();
        raspBootStrap = boot;
        RaspInfo raspInfo = new RaspInfo(EngineBoot.raspPid, EngineBoot.raspServerType, EngineBoot.VERSION);
        raspConfig.setRaspInfo(raspInfo);
        ThreadPool.exec(new UpdateRaspConfigJob());
        ThreadPool.exec(new SendRaspEventLogJob());
    }

    @Override
    public void close() {
        this.socket.close();
        this.context.term();
    }

    public String sendAndGet(String send) {
        socket.send(send.getBytes());
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
        LogTool.info("trying to Connect to server...");
        boolean connect = false;
        if (socket != null) {
            connect = socket.connect("tcp://" + ip + ":" + port);
        }
        if (!connect) {
            LogTool.error(ErrorType.REGISTER_ERROR, "false to connect to ZeroMQService server,ip=" + ip + ", port=" + port);
        }
    }

    @Override
    public void updateRaspConfig() {
        RaspConfig configRequest = new RaspConfig();
        configRequest.setId(raspConfig.getId());
        configRequest.setRaspInfo(raspConfig.getRaspInfo());
        String raspConfigStr = new Gson().toJson(configRequest);
        LogTool.debug("heart beat msg：" + raspConfigStr);
        try {
            String val = sendAndGet(MqEnums.UPDATE.getVal());
            RaspConfig config = new Gson().fromJson(val, RaspConfig.class);
            if (config != null && config.getRaspInfo() != null && config.getRaspInfo().getPid() != null && config.getRaspInfo().getPid().equals(raspConfig.getRaspInfo().getPid())) {
                if (config.getId() != null && !config.getId().equals(raspConfig.getId())) {
                    updateRaspConfig(config);
                } else {
                    LogTool.error(ErrorType.UPDATE_DATA_ERROR, "RPC 心跳数据异常：" + val);
                }
                LogTool.debug("call_update_rasp_info response: " + val);
            }
        } catch (Exception e) {
            LogTool.error(ErrorType.UPDATE_DATA_ERROR, "RPC 心跳数据异常" + e.getMessage());
        }
    }

    @Override
    public void sendRaspEventLog(RPCMemShellEventLog memShellEventLog) {
        memShellEventLog.setRaspInfo(raspConfig.getRaspInfo());
        String eventStr = new Gson().toJson(memShellEventLog);
        LogTool.info("send log msg：" + eventStr);
        try {
            String response = sendAndGet(new Gson().toJson(memShellEventLog));
            LogTool.info("call_upload_rasp_log response: " + response);
        } catch (Exception e) {
            LogTool.error(ErrorType.UPDATE_DATA_ERROR, "sendRaspEventLog error at ZeroMQService#sendRaspEventLog", e);
        }
    }

}