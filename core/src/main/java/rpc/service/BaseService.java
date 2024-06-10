package rpc.service;


import com.endpoint.rasp.engine.EngineBoot;
import com.endpoint.rasp.engine.checker.CheckParameter;
import com.endpoint.rasp.engine.checker.CheckerManager;
import com.endpoint.rasp.engine.common.constant.MemoryShellConstant;
import com.endpoint.rasp.engine.common.constant.RaspEngineConstant;
import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;
import com.google.gson.Gson;
import rpc.ErrorCode;
import rpc.LoginSingle;
import rpc.Rasp;
import rpc.bean.RPCMemShellEventLog;
import rpc.bean.RaspConfig;
import rpc.bean.RaspInfo;
import rpc.job.SendRaspEventLogJob;
import rpc.job.UpdateRaspConfigJob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * RPC相关基础服务
 */
public class BaseService {
    private static BaseService baseService = new BaseService();
    private String channel_hash;
    //TODO 需调整为配置
    private String ip = "127.0.0.1";
    private String port = "10573";
    private static RaspConfig raspConfig = new RaspConfig();
    private EngineBoot engineBoot;
    private int warning_times = 0;
    public static void main(String[] args) {
//        baseService.init(null,System.getProperty("user.dir")+ File.separator+"lib"+File.separator);

    }

    /**
     * 返回唯一实例
     * @return
     */
    public static BaseService getInstance(){
        return  baseService;
    }
    /**
     * init
     */
    public void init(EngineBoot boot,String libpath) {
        boolean is_jvm_64 = "64".equals(System.getProperty("sun.arch.data.model"))?true:false;
        System.out.println(libpath);
        if (IS_WIN32()) {
            if(is_jvm_64){
                System.load(libpath+"win\\x64\\rpc.dll");
                System.load(libpath+"win\\x64\\rpc_jni.dll");
            }else{
                System.load(libpath+"win\\x32\\rpc.dll");
                System.load(libpath+"win\\x32\\rpc_jni.dll");
            }
        } else if (IS_LINUX()) {
            if(is_jvm_64){
//                String base = System.getProperty("user.dir");
//                System.out.println(base);
//                System.load(base+"/librpc.so");
//                System.load(base+"/librpc_jni.so");
//                System.setProperty("java.library.path",System.getProperty("java.library.path")+";"+libpath+"linux/x64/");
                System.load(libpath+"linux/x64/librpc.so");
                System.load(libpath+"linux/x64/librpc_jni.so");
            }else {
                System.load(libpath+"linux/x32/librpc.so");
                System.load(libpath+"linux/x32/librpc_jni.so");
            }
        }
        engineBoot = boot;
        RaspInfo raspInfo = new RaspInfo(EngineBoot.raspPid,EngineBoot.raspServerType,EngineBoot.VERSION);
        raspConfig.setRaspInfo(raspInfo);
        login();//初始化执行登陆
        //TODO 待修改为线程池
        new Thread(new UpdateRaspConfigJob()).start();
        new Thread(new SendRaspEventLogJob()).start();
    }


    /**
     * 是否是WIN32
     *
     * @return true/false
     */
    public static boolean IS_WIN32() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * 是否是Linux
     *
     * @return true/false
     */
    public static boolean IS_LINUX() {
        return System.getProperty("os.name").contains("Linux");
    }

    /**
     * 登录RPC，连接Agent
     */
    public void login() {
        //TODO 修改线程池
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginSingle login = new LoginSingle();
                    Semaphore semp = new Semaphore(0);
                    login.login_async(ip + ":" + port, baseService, "cb_login_success", "cb_login_fails",
                            "cb_channel_disconnect", semp);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 登录成功回调
     *
     * @param channelHash
     * @param ctx
     */
    public void cb_login_success(String channelHash, Object ctx) {
        LogTool.info("login_center_success:" + channelHash);
        warning_times=0;
        channel_hash = channelHash;
    }

    /**
     * 更新Rasp配置
     */
    public void updateRaspConfig(){
        RaspConfig configRequest = new RaspConfig();
        configRequest.setId(raspConfig.getId());
        configRequest.setRaspInfo(raspConfig.getRaspInfo());
        String raspConfigStr = new Gson().toJson(configRequest);
        LogTool.debug("heart beat msg："+raspConfigStr);
        try {
            Rasp rasp = new Rasp();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            long rt = rasp.call_update_rasp_info(channel_hash,raspConfigStr.getBytes(Charset.forName("UTF-8")),stream);
            if (ErrorCode.isFail(rt)) {
                LogTool.error(ErrorType.HEARTBEAT_ERROR,ErrorCode.desc(rt));
            }else{
                byte[] bytes = stream.toByteArray();
                RaspConfig config = new Gson().fromJson(byteToString(bytes),RaspConfig.class);
                if(config!=null&&config.getRaspInfo()!=null&&config.getRaspInfo().getPid()!=null&&config.getRaspInfo().getPid().equals(raspConfig.getRaspInfo().getPid())){
                    if(config.getId()!=null&&!config.getId().equals(raspConfig.getId())){
                        raspConfig.setId(config.getId());
                        if(raspConfig.getRaspInfo()!=null&&raspConfig.getRaspInfo().getServerType()==null&&config.getRaspInfo()!=null&&config.getRaspInfo().getServerType()!=null){
                            raspConfig.getRaspInfo().setServerType(config.getRaspInfo().getServerType());
                        }
                        //开启关闭状态更新，需要同步变更引擎状态
                        if(config.getRaspStatus()!=null&&!config.getRaspStatus().equals(raspConfig.getRaspStatus())){
                            raspConfig.setRaspStatus(config.getRaspStatus());
                            if(RaspEngineConstant.RASP_ENGINE_STATUS_OPEN.equals(raspConfig.getRaspStatus())){
                                engineBoot.start();
                                raspConfig.getRaspInfo().setStatus(RaspEngineConstant.RASP_ENGINE_STATUS_OPEN);
                                LogTool.info("RASP 引擎状态修改为启动");
                            }else if(RaspEngineConstant.RASP_ENGINE_STATUS_CLOSE.equals(raspConfig.getRaspStatus())){
                                engineBoot.stop();
                                raspConfig.getRaspInfo().setStatus(RaspEngineConstant.RASP_ENGINE_STATUS_CLOSE);
                                LogTool.info("RASP 引擎状态修改为关闭");
                            }else {
                                LogTool.error(ErrorType.UPDATE_DATA_ERROR,"Rasp Status 数据异常："+raspConfig.getRaspStatus());
                            }
                        }
                        //更新白名单列表
                        raspConfig.updateMemShellWhiteConfig(config.getMemShellWhiteConfig());

                        //更新防护状态，并修改检测引擎的开关
                        if(config.getProtectStatus()!=null&&!config.getProtectStatus().equals(raspConfig.getProtectStatus())){
                            raspConfig.setProtectStatus(config.getProtectStatus());
                            if(MemoryShellConstant.ACTION_WARNING.equals(raspConfig.getProtectStatus())){
                                CheckerManager.updateBlockStatus(CheckParameter.Type.MEMORYSHELL,false);
                                raspConfig.getRaspInfo().setProtectStatus(MemoryShellConstant.ACTION_WARNING);
                                LogTool.info("RASP 引擎修改为仅检测模式");
                            }else if(MemoryShellConstant.ACTION_BLOCK.equals(raspConfig.getProtectStatus())){
                                CheckerManager.updateBlockStatus(CheckParameter.Type.MEMORYSHELL,true);
                                raspConfig.getRaspInfo().setProtectStatus(MemoryShellConstant.ACTION_BLOCK);
                                LogTool.info("RASP 引擎修改为阻断模式");
                            }else {
                                LogTool.error(ErrorType.UPDATE_DATA_ERROR,"Rasp Protect Status 数据异常："+raspConfig.getProtectStatus());
                            }
                        }
                    }
                }else{
                    LogTool.error(ErrorType.UPDATE_DATA_ERROR,"RPC 心跳数据异常："+byteToString(bytes));
                }
                LogTool.debug("call_update_rasp_info response: "+byteToString(bytes));
            }
        } catch (Exception e) {
            LogTool.error(ErrorType.UPDATE_DATA_ERROR,"RPC 心跳数据异常"+e.getMessage());
        }
    }
    /**
     * 发送Rasp告警
     */
    public void sendRaspEventLog(RPCMemShellEventLog memShellEventLog){
        memShellEventLog.setRaspInfo(raspConfig.getRaspInfo());
        String eventStr = new Gson().toJson(memShellEventLog);
        LogTool.info("send log msg："+eventStr);
        try {
            Rasp rasp = new Rasp();
            ByteArrayOutputStream stream  = new ByteArrayOutputStream();
            long rt = rasp.call_upload_rasp_log(channel_hash,eventStr.getBytes(Charset.forName("UTF-8")),stream);
            if (ErrorCode.isFail(rt)) {
                LogTool.error(ErrorType.UPLOAD_LOG_ERROR,ErrorCode.desc(rt));
            }
            byte[] bytes = stream.toByteArray();
            LogTool.info("call_upload_rasp_log response: "+byteToString(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 字节转String字符（编码UTF-8）
     * @param bytes
     * @return
     */
    private static String byteToString (byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return "";
        }
        String strContent = "";
        try {
            strContent = new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strContent;
    }

    /**
     * 登录失败回调
     *
     * @param rt
     * @param ctx
     */
    public void cb_login_fails(long rt, Object ctx) {
        if(warning_times==0){
            LogTool.info("login_center_fails:" + ErrorCode.desc(rt));
        }
        warning_times++;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogTool.info("线程休眠失败");
        }
        login();
    }

    /**
     * 与中心连接中断回调
     *
     * @param rt
     * @param channelHash
     * @param ctx
     */
    public void cb_channel_disconnect(long rt, String channelHash, Object ctx) {
        channel_hash = null;
        LogTool.warn("channel_disconnect:" + channelHash + "," + ErrorCode.desc(rt));
        // 异步执行登录
        login();
    }


    /**
     * @return channel_hash，用于调用中心-rpc方法
     */
    public String getRpcChannelHash() {
        if (channel_hash != null && !"".equals(channel_hash)) {
            return channel_hash;
        }
        return null;
    }

    /**
     * 将RPC数据转化为Json
     *
     * @param post_data
     * @return
     * @throws IOException
     */
/*
    private String changeDataToJson(String post_data) throws IOException {
        post_data = post_data.substring(post_data.indexOf("=") + 1);
        BASE64Decoder decoder = new BASE64Decoder();
        return new String(decoder.decodeBuffer(post_data), "utf-8");
    }
*/

    /**
     * RPC异步返回数据
     *
     * @param machineId
     * @param api
     * @param post_data
     * @param ctx
     * @return
     * @throws Exception
     */
    public String cb_post_data(String machineId, String api, String post_data, Object ctx) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        if ("receiveLog2".equals(api)) {//新版接收日志
            try {
                //String json = changeDataToJson(post_data);
//               LogTool.info("日志json:"+json);
                LogTool.info(post_data);
                result.put("errorcode", 0);
                result.put("msg", "成功");
            } catch (Exception e) {
                result.put("errorcode", 5);
                result.put("msg", "系统接受信息失败");
            }
        } else {
            result.put("errorcode", 5);
            result.put("msg", "没有对应的API");
        }
        return new Gson().toJson(result);
    }

    public String getIp() {
        return ip;
    }

    public String getChannel_hash() {
        return channel_hash;
    }

    public String getPort() {
        return port;
    }

    /**
     * 获取当前RASP引擎配置
     * @return
     */
    public  RaspConfig getRaspConfig(){
        return raspConfig;
    }
}
