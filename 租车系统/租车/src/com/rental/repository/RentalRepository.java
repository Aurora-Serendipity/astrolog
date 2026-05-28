
package com.rental.repository;

import com.rental.model.RentalRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 租赁记录数据仓库
 * 负责租赁记录数据的JDBC数据库持久化操作
 *
 * @author 系统
 * @version 2.0
 */
public class RentalRepository {

    /**
     * 查找所有租赁记录
     * @return 租赁记录列表
     */
    public List<RentalRecord> findAll() {
        List<RentalRecord> records = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "SELECT id, vehicle_id, vehicle_model, vehicle_type, customer_name, " +
                         "daily_rent, days, total_fee, rental_date, return_date, status " +
                         "FROM rental_records ORDER BY rental_date DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRentalRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询租赁记录列表失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }

        return records;
    }

    /**
     * 根据车辆ID查找租赁记录
     * @param vehicleId 车辆ID
     * @return 租赁记录列表
     */
    public List<RentalRecord> findByVehicleId(String vehicleId) {
        List<RentalRecord> records = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "SELECT id, vehicle_id, vehicle_model, vehicle_type, customer_name, " +
                         "daily_rent, days, total_fee, rental_date, return_date, status " +
                         "FROM rental_records WHERE vehicle_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, vehicleId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRentalRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("根据车辆ID查询租赁记录失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }

        return records;
    }

    /**
     * 查找车辆的当前租赁记录
     * @param vehicleId 车辆ID
     * @return 租赁记录，未找到返回null
     */
    public RentalRecord findActiveRental(String vehicleId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "SELECT id, vehicle_id, vehicle_model, vehicle_type, customer_name, " +
                         "daily_rent, days, total_fee, rental_date, return_date, status " +
                         "FROM rental_records WHERE vehicle_id = ? AND status = '租用中'";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, vehicleId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRentalRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("查找车辆当前租赁记录失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }

        return null;
    }

    /**
     * 保存租赁记录
     * @param record 租赁记录
     */
    public void save(RentalRecord record) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "INSERT INTO rental_records " +
                         "(id, vehicle_id, vehicle_model, vehicle_type, customer_name, " +
                         "daily_rent, days, total_fee, rental_date, return_date, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, record.getId());
            stmt.setString(2, record.getVehicleId());
            stmt.setString(3, record.getVehicleModel());
            stmt.setString(4, record.getVehicleType());
            stmt.setString(5, record.getCustomerName());
            stmt.setDouble(6, record.getDailyRent());
            stmt.setInt(7, record.getDays());
            stmt.setInt(8, record.getTotalFee());
            stmt.setLong(9, record.getRentalDate());
            stmt.setObject(10, record.getReturnDate());
            stmt.setString(11, record.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存租赁记录失败: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * 更新租赁记录
     * @param record 租赁记录
     */
    public void update(RentalRecord record) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "UPDATE rental_records SET " +
                         "vehicle_id = ?, vehicle_model = ?, vehicle_type = ?, customer_name = ?, " +
                         "daily_rent = ?, days = ?, total_fee = ?, rental_date = ?, " +
                         "return_date = ?, status = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, record.getVehicleId());
            stmt.setString(2, record.getVehicleModel());
            stmt.setString(3, record.getVehicleType());
            stmt.setString(4, record.getCustomerName());
            stmt.setDouble(5, record.getDailyRent());
            stmt.setInt(6, record.getDays());
            stmt.setInt(7, record.getTotalFee());
            stmt.setLong(8, record.getRentalDate());
            stmt.setObject(9, record.getReturnDate());
            stmt.setString(10, record.getStatus());
            stmt.setString(11, record.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新租赁记录失败: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * 删除租赁记录
     * @param id 记录ID
     */
    public void delete(String id) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            String sql = "DELETE FROM rental_records WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除租赁记录失败: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * 将ResultSet映射为RentalRecord对象
     * @param rs ResultSet对象
     * @return RentalRecord对象
     * @throws SQLException 如果映射失败
     */
    private RentalRecord mapResultSetToRentalRecord(ResultSet rs) throws SQLException {
        RentalRecord record = new RentalRecord();
        record.setId(rs.getString("id"));
        record.setVehicleId(rs.getString("vehicle_id"));
        record.setVehicleModel(rs.getString("vehicle_model"));
        record.setVehicleType(rs.getString("vehicle_type"));
        record.setCustomerName(rs.getString("customer_name"));
        record.setDailyRent(rs.getDouble("daily_rent"));
        record.setDays(rs.getInt("days"));
        record.setTotalFee(rs.getInt("total_fee"));
        record.setRentalDate(rs.getLong("rental_date"));
        record.setReturnDate(rs.getObject("return_date") != null ? rs.getLong("return_date") : null);
        record.setStatus(rs.getString("status"));
        return record;
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
            String sql = "SELECT COUNT(*) FROM rental_records";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查租赁记录数据存在性失败: " + e.getMessage());
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
}
