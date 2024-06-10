package rpc.job;

import rpc.service.BaseService;

import java.util.concurrent.TimeUnit;

/**
 * 轮询发送心跳，并按需更新配置
 *
 * Created by yunchao.zheng on 2023-10-13
 */
public class UpdateRaspConfigJob implements Runnable{
    @Override
    public void run() {
        BaseService baseService = BaseService.getInstance();
        while (true){
            try {
                TimeUnit.SECONDS.sleep(5);
                if(baseService.getRpcChannelHash()!=null){
                    baseService.updateRaspConfig();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
