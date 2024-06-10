package rpc.job;

import rpc.bean.RPCMemShellEventLog;
import rpc.service.BaseService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 发送Rasp事件日志
 *
 * Created by yunchao.zheng on 2023-10-13
 */
public class SendRaspEventLogJob implements Runnable{

    /**
     * 最多缓存1000条日志
     */
    private static LinkedBlockingQueue<RPCMemShellEventLog> queue = new LinkedBlockingQueue(1000);

    /**
     * 添加事件日志
     * @param log
     */
    public static synchronized void addLog(RPCMemShellEventLog log){
        try {
            if(!queue.offer(log)){
                queue.remove();
                queue.offer(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        BaseService baseService = BaseService.getInstance();
        while (true){
            try {
                if(baseService.getRpcChannelHash()!=null){
                    int n ;
                    if(queue!=null&&(n=queue.size())>0){
                        for(int i=0;i<n;i++){
                            RPCMemShellEventLog log = queue.poll();
                            if(log!=null){
                                baseService.sendRaspEventLog(log);
                            }
                        }
                    }
                }
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
