package com.endpoint.rasp.engine.common.log;

/**
 * 异常类型枚举
 *
 * Created by yunchao.zheng on 2023-03-17
 */
public enum ErrorType {
    ATTACK_COMMAND_WARNNING(10001,"[ATTACK][COMMAND]Suspicious command execution"),
    ATTACK_MEMORY_SHELL_WARNNING(10002,"[ATTACK][MEMORYSHELL]Suspicious memory shell execution"),
    RUNTIME_ERROR(20002, "E-RASP Engine Running Error"),
    CONFIG_ERROR(20004, "E-RASP Engine Local Config Load Error"),
    PLUGIN_ERROR(20005, "E-RASP Engine Plugin Error"),
    HOOK_ERROR(20007, "E-RASP Engine Hook Error"),
    UPDATE_DATA_ERROR(20006, "E-RASP Engine Update Data Failed"),
    REGISTER_ERROR(20008, "E-RASP Engine Connect RPC Failed"),
    HEARTBEAT_ERROR(20009, "E-RASP Engine Send HeartBeat Failed"),
    UPLOAD_LOG_ERROR(20011, "E-RASP Engine Warning Log Upload Failed"),
    UPGRADE_ERROR(30002, "E-RASP Engine Upgrade Error");

    private int code;
    private String message;

    ErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return code + ":" + message;
    }
}
