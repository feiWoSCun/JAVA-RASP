package rpc.service;

import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;
import com.endpoint.rasp.common.ThreadPool;
import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import rpc.enums.MqEnum;
import rpc.enums.ServiceTypeEnum;
import rpc.job.SendRaspEventLogJob;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: feiwoscun
 * @date: 2024/6/14
 * @email: 2825097536@qq.com
 * @description:
 */
public class ZeroMQService extends BaseService implements ServiceStrategyHandler {
    public static final int RECONNECT_IVL = 2000;
    public static final int RECONNECT_IVL_MAX = 10000;
    private ZMQ.Socket socket;
    private ZMQ.Context context;
    private static final AtomicBoolean FLAG = new AtomicBoolean(false);


    public void init() {
        super.initIpAndAdder();
        login();
        ThreadPool.exec(new SendRaspEventLogJob());
    }

    public void testMessage() {
        String s = this.sendAndGet(MqEnum.UPLOAD_LOG);
        LogTool.info(s);
        System.out.println(s);
    }


    @Override
    public void close() {
        ThreadPool.shutdownAndAwaitTermination();
        try {
            if (!FLAG.get()) {
                throw new RuntimeException("zero mq service closed fail ,Maybe zeromq isn't connected yet ");
            }
            if (socket != null) {
                this.socket.close();
            }
            //todo 如果执行rasp uninstall 并且此时 socket没有连接上的话，会一直阻塞在这儿，有空看一下有没有解决方案
            if (context != null) {
                this.context.term();
            }
        } catch (Exception e) {
            LogTool.error(ErrorType.MQ_CLOSE_ERROR,
                    "【zeromq】mq close error");
        }
        //关闭线程池

    }

    @Override
    public String sendAndGet(String send) {
        socket.send(send.getBytes());
        byte[] receive = socket.recv();
        String s = new String(receive);
        LogTool.info("[zeromq]:收到数据：" + s);
        return s;
    }

    public String sendAndGet(Object send) {
        String json = new Gson().toJson(send);
        socket.send(json.getBytes());
        byte[] receive = socket.recv();
        return new String(receive);
    }


    @Override
    public void init( String libPath) {
        this.init();
    }

    @Override
    public void login() {
        context = ZMQ.context(1);
        if (!context.isClosed()) {
            LogTool.info("【zeromq】: Trying to connect to server... IP: " + ip + ", Port: " + port);
            try {
                socket = context.socket(SocketType.REQ);
                if (socket != null) {
                    boolean connect = socket.connect("tcp://" + ip + ":" + port);
                    if (connect && FLAG.compareAndSet(false, true)) {
                        socket.setReconnectIVL(RECONNECT_IVL);
                        socket.setReconnectIVLMax(RECONNECT_IVL_MAX);
                        LogTool.info("【zeromq】 Successfully connected to ZeroMQService server.");
                    } else {
                        context.close();
                        String msg = "【zeromq】 Failed to connect to ZeroMQService server, IP=" + ip + ", Port=" + port;
                        LogTool.error(ErrorType.REGISTER_ERROR, msg);
                        throw new RuntimeException(msg);
                    }

                }

            } catch (ZMQException e) {
                LogTool.error(ErrorType.REGISTER_ERROR, "【zeromq】 Exception while connecting to ZeroMQService server, IP=" + ip + ", Port=" + port, e);
            }
        }
    }

    @Override
    public String getRpcChannelHash() {
        return "zeromq";
    }


    @Override
    public ServiceTypeEnum getServiceType() {
        return ServiceTypeEnum.MQ;
    }
}