
package com.rental.repository;

import com.rental.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 车辆数据仓库
 * 负责车辆数据的JDBC数据库持久化操作
 *
 * @author 系统
 * @version 3.0
 */
public class VehicleRepository {

    /**
     * 查找所有车辆
     * @return 车辆列表
     */
    public List<Vehicle> findAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "SELECT id, type, model, daily_rent, status FROM vehicles";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询车辆列表失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }

        return vehicles;
    }

    /**
     * 根据ID查找车辆
     * @param id 车辆ID
     * @return 车辆对象，未找到返回null
     */
    public Vehicle findById(String id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "SELECT id, type, model, daily_rent, status FROM vehicles WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToVehicle(rs);
            }
        } catch (SQLException e) {
            System.err.println("根据ID查询车辆失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }

        return null;
    }

    /**
     * 根据状态查找车辆
     * @param status 车辆状态
     * @return 车辆列表
     */
    public List<Vehicle> findByStatus(String status) {
        List<Vehicle> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "SELECT id, type, model, daily_rent, status FROM vehicles WHERE status = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();

            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据状态查询车辆失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }

        return vehicles;
    }

    /**
     * 保存车辆
     * @param vehicle 车辆对象
     */
    public void save(Vehicle vehicle) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "INSERT INTO vehicles (id, type, model, daily_rent, status) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, vehicle.getId());
            stmt.setString(2, vehicle.getType());
            stmt.setString(3, vehicle.getModel());
            stmt.setDouble(4, vehicle.getDailyRent());
            stmt.setString(5, vehicle.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存车辆失败: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * 更新车辆
     * @param vehicle 车辆对象
     */
    public void update(Vehicle vehicle) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "UPDATE vehicles SET type = ?, model = ?, daily_rent = ?, status = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, vehicle.getType());
            stmt.setString(2, vehicle.getModel());
            stmt.setDouble(3, vehicle.getDailyRent());
            stmt.setString(4, vehicle.getStatus());
            stmt.setString(5, vehicle.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新车辆失败: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * 删除车辆
     * @param id 车辆ID
     */
    public void delete(String id) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "DELETE FROM vehicles WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除车辆失败: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * 保存所有车辆
     * @param vehicles 车辆列表
     */
    public void saveAll(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            save(vehicle);
        }
    }

    /**
     * 将ResultSet映射为Vehicle对象
     * @param rs ResultSet对象
     * @return Vehicle对象
     * @throws SQLException 如果映射失败
     */
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
            rs.getString("id"),
            rs.getString("type"),
            rs.getString("model"),
            rs.getDouble("daily_rent"),
            rs.getString("status")
        );
    }

    /**
     * 关闭数据库资源
     * @param rs ResultSet
     * @param stmt Statement
     * @param conn Connection
     */
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("关闭 ResultSet 失败: " + e.getMessage());
            }
        }
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

    /**
     * 检查数据是否存在
     * @return true表示存在数据
     */
    public boolean dataExists() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "SELECT COUNT(*) FROM vehicles";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查数据存在性失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }

        return false;
    }

    /**
     * 检查数据文件是否存在（兼容旧接口）
     * @return true表示文件存在
     * @deprecated 请使用 dataExists() 方法
     */
    @Deprecated
    public boolean dataFileExists() {
        return dataExists();
    }

    /**
     * 从文件加载车辆列表（兼容旧接口）
     * @return 车辆列表
     * @deprecated 请使用 findAll() 方法
     */
    @Deprecated
    public List<Vehicle> loadVehicles() {
        return findAll();
    }

    /**
     * 保存车辆列表到文件（兼容旧接口）
     * @param vehicles 车辆列表
     * @deprecated 请使用 saveAll() 方法
     */
    @Deprecated
    public void saveVehicles(List<Vehicle> vehicles) {
        saveAll(vehicles);
    }
}
