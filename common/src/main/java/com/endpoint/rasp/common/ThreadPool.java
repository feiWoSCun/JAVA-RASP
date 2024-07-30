package com.endpoint.rasp.common;

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
            //todo 现在有一个bug，就是说线程池的关闭并不会关闭线程，就是job包里的死循环不会退出，很奇怪。目前的解决方案是设置成守护线程
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler(GlobalUncaughtExceptionHandler.getInstance());
            return thread;
        }
    }

    static class GlobalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private static final GlobalUncaughtExceptionHandler INSTANCE = new GlobalUncaughtExceptionHandler();

        private GlobalUncaughtExceptionHandler() {
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            AnsiLog.error("Exception in thread " + t.getName(), e);
        }

        /**
         * 单例模式
         */
        public static GlobalUncaughtExceptionHandler getInstance() {
            return INSTANCE;
        }

    }

    static {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 30, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(5));
        executor.setThreadFactory(new ThreadFactoryAutoLogError(Executors.defaultThreadFactory()));
        threadPoolExecutor = executor;
        // 注册关闭钩子
/*        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogTool.info("Shutdown hook triggered.");
            shutdownAndAwaitTermination();
        }));*/
    }

    public static void shutdownAndAwaitTermination() {
        // Disable new tasks from being submitted
        ((ExecutorService) ThreadPool.threadPoolExecutor).shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!((ExecutorService) ThreadPool.threadPoolExecutor).awaitTermination(1, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                ((ExecutorService) ThreadPool.threadPoolExecutor).shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!((ExecutorService) ThreadPool.threadPoolExecutor).awaitTermination(1, TimeUnit.SECONDS)) {
                    AnsiLog.info("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            ((ExecutorService) ThreadPool.threadPoolExecutor).shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static <T> T submitAndGet(Callable<T> task) throws TimeoutException, InterruptedException, ExecutionException {
        Future<T> future = threadPoolExecutor.submit(task);
        try {
            // 等待10秒
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            AnsiLog.error("Task timed out");
            throw e;
        } catch (InterruptedException e) {
            AnsiLog.error("Task was interrupted");
            throw e;
        } catch (ExecutionException e) {
            AnsiLog.error("Task encountered an exception: " + e.getCause());
            throw e;
        }
    }

    public static void exec(Runnable r) {
        threadPoolExecutor.execute(r);
    }

}
