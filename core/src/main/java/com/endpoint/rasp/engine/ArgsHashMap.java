package com.endpoint.rasp.engine;

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

    public ArgsHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public void setAgentPath() {
        this.put(AGENT, encodeArg(this.get(AGENT)));
    }

    public void setCorePath() {
        this.put(CORE, encodeArg(this.get(CORE)));
    }

    private static String encodeArg(String arg) {
        try {
            return URLEncoder.encode(arg, "utf-8");
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

    public  void printSelf(){
        Set<Entry<String, String>> entries = this.entrySet();
        for (Entry<String, String> entry : entries) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
