package rpc.bean;

/**
 * 上报事件日志
 * Created by yunchao.zheng on 2023-10-12
 */
public class RPCMemShellEventLog extends CommandFlag {

    /**
     * 调用栈追踪
     */
    private String[] stackTrace;
    /**
     * 类文件路径
     */
    private String classPath;
    /**
     * 类名称
     */
    private String className;
    /**
     * 告警消息
     */
    private String message;
    /**
     * 是否阻断：true（阻断），false
     */
    private boolean block;
    /**
     * 引擎基本信息
     */
    private RaspInfo raspInfo;

    public RPCMemShellEventLog(){}

    /**
     *
     * @param block         是否阻断
     * @param message       告警消息
     * @param className     类名
     * @param classPath     类路径
     * @param stackTrace    函数调用栈
     */
    public RPCMemShellEventLog(boolean block,String message, String className, String classPath, String[] stackTrace){
        this.block = block;
        this.message = message;
        this.className = className;
        this.classPath = classPath;
        this.stackTrace = stackTrace;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public RaspInfo getRaspInfo() {
        return raspInfo;
    }

    public void setRaspInfo(RaspInfo raspInfo) {
        this.raspInfo = raspInfo;
    }
}
