package rpc.job;

import com.endpoint.rasp.engine.common.log.LogTool;
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
    private static final LinkedBlockingQueue<RPCMemShellEventLog> queue = new LinkedBlockingQueue<>(1000);

    /**
     * 添加事件日志
     * @param log
     */
    public static synchronized void addLog(RPCMemShellEventLog log) {
        if (!queue.offer(log)) {
            queue.remove();
            queue.offer(log);
        }
    }

    @Override
    public void run() {
        BaseService baseService = BaseService.getInstance();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (baseService.getRpcChannelHash() != null) {
                    int n;
                    if ((n = queue.size()) > 0) {
                        for (int i = 0; i < n; i++) {
                            RPCMemShellEventLog log = queue.poll();
                            baseService.sendRaspEventLog(log);
                        }
                    }
                }
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
