package com.astrolog.util;

import com.astrolog.config.DBConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DBUtil {
    private static volatile DBUtil instance;
    private final BlockingQueue<Connection> pool;
    private DBConfig config;
    private volatile boolean shutdown = false;

    private DBUtil() {
        config = loadConfig();
        pool = new ArrayBlockingQueue<>(config.getMaxConnections());
        initPool();
    }

    public static DBUtil getInstance() {
        if (instance == null) {
            synchronized (DBUtil.class) {
                if (instance == null) {
                    instance = new DBUtil();
                }
            }
        }
        return instance;
    }

    private DBConfig loadConfig() {
        Properties props = new Properties();
        try {
            props.load(DBUtil.class.getClassLoader().getResourceAsStream("db.properties"));

            DBConfig cfg = new DBConfig();
            cfg.setDriverClass(props.getProperty("jdbc.driver"));
            cfg.setJdbcUrl(props.getProperty("jdbc.url"));
            cfg.setUsername(props.getProperty("jdbc.username"));
            cfg.setPassword(props.getProperty("jdbc.password"));
            cfg.setMaxConnections(Integer.parseInt(props.getProperty("pool.maxConnections", "10")));
            cfg.setMinConnections(Integer.parseInt(props.getProperty("pool.minConnections", "2")));
            cfg.setInitialConnections(Integer.parseInt(props.getProperty("pool.initialConnections", "5")));

            Class.forName(cfg.getDriverClass());
            return cfg;
        } catch (Exception e) {
            throw new RuntimeException("加载数据库配置失败: " + e.getMessage(), e);
        }
    }

    private void initPool() {
        for (int i = 0; i < config.getInitialConnections(); i++) {
            try {
                Connection conn = createConnection();
                pool.offer(conn);
            } catch (SQLException e) {
                System.err.println("初始化连接池失败: " + e.getMessage());
            }
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            config.getJdbcUrl(), config.getUsername(), config.getPassword());
    }

    public Connection getConnection() throws SQLException {
        if (shutdown) {
            throw new SQLException("连接池已关闭");
        }
        try {
            Connection conn = pool.poll();
            if (conn == null) {
                if (getActiveCount() < config.getMaxConnections()) {
                    return createConnection();
                } else {
                    conn = pool.take();
                }
            }
            if (conn.isClosed() || !conn.isValid(5)) {
                return getConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断", e);
        }
    }

    public void releaseConnection(Connection conn) {
        if (conn != null && !shutdown) {
            try {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(true);
                    pool.offer(conn);
                }
            } catch (SQLException e) {
                System.err.println("归还连接失败: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        shutdown = true;
        while (!pool.isEmpty()) {
            Connection conn = pool.poll();
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("关闭连接失败: " + e.getMessage());
                }
            }
        }
    }

    public int getActiveCount() {
        return config.getMaxConnections() - pool.size();
    }

    public int getIdleCount() {
        return pool.size();
    }
}
