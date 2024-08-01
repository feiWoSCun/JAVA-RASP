package rpc.job;


import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;
import org.apache.log4j.Logger;
import rpc.bean.RPCMemShellEventLog;
import rpc.service.BaseService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 发送Rasp事件日志
 * <p>
 * Created by yunchao.zheng on 2023-10-13
 */
public class SendRaspEventLogJob implements Runnable {

    private static final Logger log = Logger.getLogger(SendRaspEventLogJob.class);
    /**
     * 最多缓存1000条日志
     */
    private static final LinkedBlockingQueue<RPCMemShellEventLog> QUEUE = new LinkedBlockingQueue<>(1000);

    /**
     * 添加事件日志
     * @param log
     */
    public static synchronized void addLog(RPCMemShellEventLog log) {
        if (!QUEUE.offer(log)) {
            QUEUE.remove();
            QUEUE.offer(log);
        }
    }

    @Override
    public void run() {
        BaseService baseService = BaseService.getInstance();
        while (!Thread.interrupted()) {
            try {
                if (baseService.getRpcChannelHash() != null) {
                    int n;
                    if ((n = QUEUE.size()) > 0) {
                        for (int i = 0; i < n; i++) {
                            RPCMemShellEventLog log = QUEUE.poll();
                            baseService.sendRaspEventLog(log);
                        }
                    }
                }
                TimeUnit.SECONDS.sleep(3*60);
            } catch (InterruptedException e) {
                LogTool.error(ErrorType.UPLOAD_LOG_ERROR, "线程打断,可能是因为触发卸载", e);
                break;
            }
        }
    }
}
