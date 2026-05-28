
package com.rental.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * JDBC连接管理器
 * 实现简单的数据库连接池管理
 * 
 * @author 系统
 * @version 1.0
 */
public class DBConnectionManager {
    private static DBConnectionManager instance;
    private BlockingQueue<Connection> connectionPool;
    private Properties properties;
    
    /** 最大连接数 */
    private int maxConnections;
    
    /** 最小连接数 */
    private int minConnections;
    
    /** 数据库驱动类名 */
    private String driverClass;
    
    /** 数据库连接URL */
    private String jdbcUrl;
    
    /** 数据库用户名 */
    private String username;
    
    /** 数据库密码 */
    private String password;
    
    /**
     * 私有构造函数，初始化连接池
     */
    private DBConnectionManager() {
        loadProperties();
        initConnectionPool();
    }
    
    /**
     * 获取单例实例
     * @return DBConnectionManager 实例
     */
    public static synchronized DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        return instance;
    }
    
    /**
     * 加载数据库配置文件
     */
    private void loadProperties() {
        properties = new Properties();
        try {
            properties.load(DBConnectionManager.class.getClassLoader().getResourceAsStream("db.properties"));
            
            driverClass = properties.getProperty("jdbc.driver");
            jdbcUrl = properties.getProperty("jdbc.url");
            username = properties.getProperty("jdbc.username");
            password = properties.getProperty("jdbc.password");
            
            maxConnections = Integer.parseInt(properties.getProperty("pool.maxConnections", "10"));
            minConnections = Integer.parseInt(properties.getProperty("pool.minConnections", "2"));
            
            // 加载驱动类
            Class.forName(driverClass);
        } catch (Exception e) {
            throw new RuntimeException("加载数据库配置失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 初始化连接池
     */
    private void initConnectionPool() {
        connectionPool = new ArrayBlockingQueue<>(maxConnections);
        
        // 初始化最小连接数
        for (int i = 0; i < minConnections; i++) {
            try {
                Connection conn = createConnection();
                connectionPool.offer(conn);
            } catch (SQLException e) {
                System.err.println("初始化连接池失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 创建新的数据库连接
     * @return Connection 对象
     * @throws SQLException 如果连接失败
     */
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
    
    /**
     * 从连接池获取连接
     * @return Connection 对象
     * @throws SQLException 如果获取连接失败
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll();
            if (conn == null) {
                // 如果连接池为空，创建新连接
                if (getActiveConnections() < maxConnections) {
                    return createConnection();
                } else {
                    // 等待可用连接
                    conn = connectionPool.take();
                }
            }
            
            // 检查连接是否有效
            if (conn.isClosed() || !conn.isValid(5)) {
                return getConnection();
            }
            
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断", e);
        }
    }
    
    /**
     * 归还连接到连接池
     * @param conn Connection 对象
     */
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    // 重置连接状态
                    conn.setAutoCommit(true);
                    connectionPool.offer(conn);
                }
            } catch (SQLException e) {
                System.err.println("归还连接失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 关闭连接（不归还到连接池）
     * @param conn Connection 对象
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("关闭连接失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取当前活跃连接数
     * @return 活跃连接数
     */
    public int getActiveConnections() {
        return maxConnections - connectionPool.size();
    }
    
    /**
     * 关闭连接池中的所有连接
     */
    public void shutdown() {
        while (!connectionPool.isEmpty()) {
            Connection conn = connectionPool.poll();
            closeConnection(conn);
        }
    }
}
