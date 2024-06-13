package com.endpoint.rasp.engine;

import com.endpoint.rasp.common.AnsiLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description: 用来保存参数java -jar的参数信息并且提供一些工具方法
 */
public class ArgsHashMap extends HashMap<String, String> {

    public static final String AGENT = "-agent";
    public static final String CORE = "-core";
    public static final String PID = "-pid";
    public static final String UTF_8 = "utf-8";

    public ArgsHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    //下面两个方法，应该是有用的，但是不用好像没什么毛病
    public void setAgentPath() {
        this.put(AGENT, encodeArg(this.get(AGENT)));
    }

    public void setCorePath() {
        this.put(CORE, encodeArg(this.get(CORE)));
    }

    private static String encodeArg(String arg) {
        try {
            return URLEncoder.encode(arg, UTF_8);
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }

    public String getAgentPath() {
        return this.get(AGENT);
    }

    public String getCorePath() {
        return this.get(CORE);
    }

    public void printSelf() {
        AnsiLog.info("in core.jar,use these options:");
        Set<Entry<String, String>> entries = this.entrySet();
        for (Entry<String, String> entry : entries) {
            AnsiLog.info(entry.getKey() + ": " + entry.getValue());
        }
    }

    public String getPid() {
        return this.get(PID);
    }
}
