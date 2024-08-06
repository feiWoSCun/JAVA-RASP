package rpc.enums;

/**
 * @author: feiwoscun
 * @date: 2024/6/14
 * @email: 2825097536@qq.com
 * @description:
 */
public enum MqEnum {
    UPDATE_RASP_INFO("update_rasp_info", "request to update RaspConfig......"),

    UPLOAD_LOG("upload_rasp_log", "zero mq message transformer test.....");

    private final String desc;
    private final String val;

    public String getDesc() {
        return desc;
    }

    public String getVal() {
        return val;
    }

    MqEnum(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }
}
