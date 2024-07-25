package rpc.job;

import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import rpc.service.BaseService;

import java.util.concurrent.TimeUnit;

/**
 * 轮询发送心跳，并按需更新配置
 * <p>
 */
public class UpdateRaspConfigJob implements Runnable {
    @Override
    public void run() {
        BaseService baseService = BaseService.getInstance();
        while (!Thread.interrupted()) {
            try {
                TimeUnit.SECONDS.sleep(5);
                if (baseService.getRpcChannelHash() != null) {
                    baseService.updateRaspConfig();
                }
            } catch (InterruptedException e) {
                LogTool.error(ErrorType.HEARTBEAT_ERROR, "线程打断,可能是因为触发卸载", e);
                break;
            }
        }
    }
}
