package rpc.bean;

/**
 * Rasp引擎基本信息
 * Created by yunchao.zheng on 2023-10-11
 */
public class RaspInfo {
    /**
     * 当前引擎服务状态，open(开启) or close(关闭)
     */
    private String status;
    /**
     * 注入进程ID
     */
    private String pid;
    /**
     * 注入容器类型:tomcat，jetty，weblogic，resin，spring（springboot\springmvc）
     */
    private String serverType;

    /**
     * 当前防护状态：warning（仅告警），block（告警并阻断）
     */
    private String protectStatus;
    /**
     * 引擎版本号
     */
    private int version;

    public RaspInfo(){}

    public RaspInfo(String pid,String serverType,int version){
        this.status = "close";
        this.pid = pid;
        this.serverType = serverType;
        this.version= version;
    }

    public RaspInfo(String status,String pid,String serverType,int version){
        this.status = status;
        this.pid = pid;
        this.serverType = serverType;
        this.version= version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getProtectStatus() {
        return protectStatus;
    }

    public void setProtectStatus(String protectStatus) {
        this.protectStatus = protectStatus;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
