package rpc.bean;

/**
 * RASP相关业务功能配置
 *
 * Created by yunchao.zheng on 2023-10-11
 */
public class RaspConfig {

    public RaspConfig(){}

    /**
     * 配置唯一标识
     */
    private String id;
    /**
     * 引擎基本信息
     */
    private RaspInfo raspInfo;
    /**
     * 引擎状态：open，close
     */
    private String raspStatus;
    /**
     * 白名单配置
     */
    private MemShellWhiteConfig memShellWhiteConfig;

    /**
     * 防护状态：warning（仅告警），block（告警并阻断）
     */
    private String protectStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RaspInfo getRaspInfo() {
        return raspInfo;
    }

    public void setRaspInfo(RaspInfo raspInfo) {
        this.raspInfo = raspInfo;
    }

    public String getRaspStatus() {
        return raspStatus;
    }

    public void setRaspStatus(String raspStatus) {
        this.raspStatus = raspStatus;
    }

    public MemShellWhiteConfig getMemShellWhiteConfig() {
        return memShellWhiteConfig;
    }

    public void setMemShellWhiteConfig(MemShellWhiteConfig memShellWhiteConfig) {
        this.memShellWhiteConfig = memShellWhiteConfig;
    }

    public String getProtectStatus() {
        return protectStatus;
    }

    public void setProtectStatus(String protectStatus) {
        this.protectStatus = protectStatus;
    }

    public synchronized void updateMemShellWhiteConfig(MemShellWhiteConfig memShellWhiteConfig){
        this.memShellWhiteConfig = memShellWhiteConfig;
    }

    /**
     * 验证是否匹配白名单,同步执行，如果存在动态添加Servlet等Web对象的框架，可能存在一定延迟。
     * @param className Class名称
     * @param classPath Class路径
     * @param checkType 检测类型
     */
    public synchronized boolean checkMemShellWhiteList(String className,String classPath,String checkType){
        if(className!=null&&memShellWhiteConfig!=null&&memShellWhiteConfig.getClassNames()!=null&&memShellWhiteConfig.getClassNames().contains(className)){
            return true;
        }
        if(classPath!=null&&memShellWhiteConfig!=null&&memShellWhiteConfig.getClassPaths()!=null&&memShellWhiteConfig.getClassPaths().contains(classPath)){
            return true;
        }
        if(checkType!=null&&memShellWhiteConfig!=null&&memShellWhiteConfig.getTypes()!=null&&memShellWhiteConfig.getTypes().contains(checkType)){
            return true;
        }
        return false;
    }
}
