package rpc.service;

import com.endpoint.rasp.engine.common.log.ErrorType;
import com.endpoint.rasp.engine.common.log.LogTool;

import java.util.concurrent.*;

/**
 * @author: feiwoscun
 * @date: 2024/6/14
 * @email: 2825097536@qq.com
 * @description: 解决子线程直接throw 异常的时候没有日志打印的问题，并封装一些方法
 */
public class ThreadPool {
    private static final int TIMEOUT = 10;
    private static final ThreadPoolExecutor threadPoolExecutor;

    static class ThreadFactoryAutoLogError implements ThreadFactory {
        private final ThreadFactory factory;

        ThreadFactoryAutoLogError(ThreadFactory factory) {
            this.factory = factory;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = factory.newThread(r);
            thread.setUncaughtExceptionHandler(GlobalUncaughtExceptionHandler.getInstance());
            return thread;
        }
    }

    static class GlobalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private static final GlobalUncaughtExceptionHandler INSTANCE = new GlobalUncaughtExceptionHandler();

        private GlobalUncaughtExceptionHandler() {
        }

        public void uncaughtException(Thread t, Throwable e) {
            LogTool.error(ErrorType.RUNTIME_ERROR, "Exception in thread " + t.getName(), e);
        }

        /**
         * 单例模式
         */
        public static GlobalUncaughtExceptionHandler getInstance() {
            return INSTANCE;
        }

    }

    static {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5));
        executor.setThreadFactory(new ThreadFactoryAutoLogError(Executors.defaultThreadFactory()));
        threadPoolExecutor = executor;
    }


    public static <T> T submitAndGet(Callable<T> task) throws TimeoutException, InterruptedException, ExecutionException {
        Future<T> future = threadPoolExecutor.submit(task);
        try {
            // 等待10秒
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LogTool.error(ErrorType.HEARTBEAT_ERROR, "Task timed out");
            throw e;
        } catch (InterruptedException e) {
            LogTool.error(ErrorType.RUNTIME_ERROR, "Task was interrupted");
            throw e;
        } catch (ExecutionException e) {
            LogTool.error(ErrorType.RUNTIME_ERROR, "Task encountered an exception: " + e.getCause());
            throw e;
        }
    }

    public static void exec(Runnable r) {
        threadPoolExecutor.execute(r);
    }
}
