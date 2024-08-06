package rpc.job;


import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;
import com.google.gson.Gson;
import rpc.service.BaseService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author feiwoscun
 */
public class SendRaspEventLogJob implements Runnable {


    private static final LinkedBlockingQueue<Object> QUEUE = new LinkedBlockingQueue<>(1000);

    /**
     * 添加事件日志
     * @param log
     */
    public static synchronized void addLog(Object log) {
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
                            Object log = QUEUE.poll();
                            String json = new Gson().toJson(log);
                            baseService.sendAndGet(json);
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
