package com.rental.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池管理器
 * 负责管理服务端处理客户端请求的线程池
 * 使用可缓存线程池，支持动态扩展
 *
 * @author 系统
 * @version 1.0
 */
public class ThreadPoolManager {
    private static final int THREAD_POOL_SIZE = 10;
    private final ExecutorService executorService;

    /**
     * 构造函数
     */
    public ThreadPoolManager() {
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * 执行任务
     * @param task 任务
     */
    public void execute(Runnable task) {
        executorService.execute(task);
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
    }
}