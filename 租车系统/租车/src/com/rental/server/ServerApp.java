package com.rental.server;

import com.rental.repository.DatabaseInitializer;
import com.rental.service.VehicleService;

/**
 * 服务端主程序
 * 启动ServerSocket，监听客户端连接
 * 使用线程池处理并发请求
 *
 * @author 系统
 * @version 2.0
 */
public class ServerApp {
    private static final int PORT = 8888;
    private static final String SERVER_NAME = "租车系统服务端";

    /**
     * 主方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("========== " + SERVER_NAME + " 启动中 ==========");

        // 初始化数据库
        initializeDatabase();

        ThreadPoolManager threadPool = new ThreadPoolManager();
        VehicleService vehicleService = new VehicleService();

        vehicleService.initializeDefaultData();
        System.out.println("车辆数据初始化完成");

        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(PORT)) {
            System.out.println("服务端启动成功，监听端口: " + PORT);
            System.out.println("等待客户端连接...");

            while (true) {
                java.net.Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("客户端连接: " + clientAddress);

                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }
        } catch (java.io.IOException e) {
            System.err.println("服务端异常: " + e.getMessage());
        } finally {
            // 注意：此处的 threadPool.shutdown() 实际上不会被执行，因为 while(true) 是死循环
            // 在实际生产环境中，应该使用信号处理器来处理关闭信号
            System.out.println("服务端已关闭");
        }
    }

    /**
     * 初始化数据库
     */
    private static void initializeDatabase() {
        try {
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();
            System.out.println("数据库初始化完成");
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
