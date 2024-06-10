package rpc.bean;

/**
 * Agent响应消息
 *
 * Created by yunchao.zheng on 2023-10-12
 */
public class AgentResponseMsg {
    /**
     * 错误消息
     */
    private String msg;
    /**
     * 0-代表成功，其他的由Agent定义。
     */
    private String code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
