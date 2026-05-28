
package com.rental.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库初始化工具类
 * 负责创建数据库表结构
 * 
 * @author 系统
 * @version 1.0
 */
public class DatabaseInitializer {
    
    /**
     * 创建车辆表 SQL
     */
    private static final String CREATE_VEHICLES_TABLE = 
        "CREATE TABLE IF NOT EXISTS vehicles (" +
        "id VARCHAR(50) PRIMARY KEY," +
        "type VARCHAR(50) NOT NULL," +
        "model VARCHAR(100) NOT NULL," +
        "daily_rent DECIMAL(10,2) NOT NULL," +
        "status VARCHAR(20) NOT NULL DEFAULT '可租赁'" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    
    /**
     * 创建租赁记录表 SQL
     */
    private static final String CREATE_RENTAL_RECORDS_TABLE = 
        "CREATE TABLE IF NOT EXISTS rental_records (" +
        "id VARCHAR(50) PRIMARY KEY," +
        "vehicle_id VARCHAR(50) NOT NULL," +
        "vehicle_model VARCHAR(100) NOT NULL," +
        "vehicle_type VARCHAR(50) NOT NULL," +
        "customer_name VARCHAR(100) NOT NULL," +
        "daily_rent DECIMAL(10,2) NOT NULL," +
        "days INT NOT NULL," +
        "total_fee INT NOT NULL," +
        "rental_date BIGINT NOT NULL," +
        "return_date BIGINT," +
        "status VARCHAR(20) NOT NULL DEFAULT '租用中'," +
        "INDEX idx_vehicle_id (vehicle_id)," +
        "INDEX idx_status (status)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    
    /**
     * 初始化数据库表
     * @throws SQLException 如果初始化失败
     */
    public void initialize() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = DBConnectionManager.getInstance().getConnection();
            stmt = conn.createStatement();
            
            // 创建车辆表
            stmt.executeUpdate(CREATE_VEHICLES_TABLE);
            System.out.println("车辆表 vehicles 创建成功");
            
            // 创建租赁记录表
            stmt.executeUpdate(CREATE_RENTAL_RECORDS_TABLE);
            System.out.println("租赁记录表 rental_records 创建成功");
            
        } finally {
            // 关闭资源
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("关闭 Statement 失败: " + e.getMessage());
                }
            }
            if (conn != null) {
                DBConnectionManager.getInstance().releaseConnection(conn);
            }
        }
    }
    
    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return true 如果表存在
     * @throws SQLException 如果查询失败
     */
    public boolean tableExists(String tableName) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = DBConnectionManager.getInstance().getConnection();
            stmt = conn.createStatement();
            
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'zuche' AND table_name = '" + tableName + "'";
            var rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("关闭 Statement 失败: " + e.getMessage());
                }
            }
            if (conn != null) {
                DBConnectionManager.getInstance().releaseConnection(conn);
            }
        }
    }
    
    /**
     * 清空所有表数据（谨慎使用）
     * @throws SQLException 如果清空失败
     */
    public void clearAllTables() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = DBConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.createStatement();
            
            // 先清空租赁记录表（外键约束）
            stmt.executeUpdate("DELETE FROM rental_records");
            // 清空车辆表
            stmt.executeUpdate("DELETE FROM vehicles");
            
            conn.commit();
            System.out.println("所有表数据已清空");
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("回滚失败: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("关闭 Statement 失败: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("重置自动提交失败: " + e.getMessage());
                }
                DBConnectionManager.getInstance().releaseConnection(conn);
            }
        }
    }
}
