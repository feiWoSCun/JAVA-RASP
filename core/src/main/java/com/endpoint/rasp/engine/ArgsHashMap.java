package com.endpoint.rasp.engine;

import com.endpoint.rasp.common.ArgsEnums;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * @author: feiwoscun
 * @date: 2024/6/9
 * @email: 2825097536@qq.com
 * @description: 用来保存参数java -jar的参数信息并且提供一些工具方法
 */
public class ArgsHashMap extends HashMap<String, String> {

    public static final String PID = "-pid";
    public static final String UTF_8 = "utf-8";
    public static final String ACTION = "-action";

    public ArgsHashMap(int initialCapacity) {
        super(initialCapacity);
    }


    private static String encodeArg(String arg) {
        try {
            return URLEncoder.encode(arg, UTF_8);
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }

    public String getAgentPath() {
        return this.get("-home") + "/agent.jar";
    }

    public String getCorePath() {
        return this.get("-home") + "/rasp-core-shade.jar";
    }

/*    @Override
    public String toString() {
        Set<Entry<String, String>> entries = this.entrySet();
        return encodeArg(this.getCorePath() + ";" +
                this.getAgentPath() + ";" +
                this.getPid() + ";" +
                this.getAction());
    }*/

    public String getPid() {
        return this.get(PID);
    }

    public String getAction() {
        return this.get(ACTION);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String s : ArgsEnums.cache.keySet()) {
            builder.append(s);
            builder.append(";");
            builder.append(this.get(s));
            builder.append(";");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
