package rpc.service;

/**
 * @author: feiwoscun
 * @date: 2024/6/14
 * @email: 2825097536@qq.com
 * @description:
 */
public enum MqEnums {
    UPDATE("1", "请求更新RaspConfig");

    private final String desc;
    private final String val;

    public String getDesc() {
        return desc;
    }

    public String getVal() {
        return val;
    }

    MqEnums(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }
}
