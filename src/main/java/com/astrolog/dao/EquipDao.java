package com.astrolog.dao;

import com.astrolog.model.Equipment;
import com.astrolog.model.enums.EquipStatus;
import com.astrolog.model.enums.EquipType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EquipDao extends BaseDao<Equipment> {

    // === 基础 CRUD ===

    public Equipment findById(int equipId) {
        String sql = "SELECT equip_id, user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description "
                   + "FROM equipment WHERE equip_id = ?";
        List<Equipment> results = executeQuery(sql, new Object[]{equipId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Equipment> findAllByUserId(int userId) {
        String sql = "SELECT equip_id, user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description "
                   + "FROM equipment WHERE user_id = ? ORDER BY name";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public int insert(Equipment equip) {
        String sql = "INSERT INTO equipment (user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            equip.getUserId(),
            equip.getName(),
            equip.getType().name().toLowerCase(),
            equip.getAperture(),
            equip.getFocalLength(),
            equip.getPurchaseDate() != null
                ? Date.valueOf(equip.getPurchaseDate()) : null,
            equip.getStatus().name().toLowerCase(),
            equip.getDescription()});
    }

    public boolean update(Equipment equip) {
        String sql = "UPDATE equipment SET name=?, type=?, aperture=?, "
                   + "focal_length=?, purchase_date=?, status=?, description=? "
                   + "WHERE equip_id=?";
        return executeUpdate(sql, new Object[]{
            equip.getName(),
            equip.getType().name().toLowerCase(),
            equip.getAperture(),
            equip.getFocalLength(),
            equip.getPurchaseDate() != null
                ? Date.valueOf(equip.getPurchaseDate()) : null,
            equip.getStatus().name().toLowerCase(),
            equip.getDescription(),
            equip.getEquipId()}) > 0;
    }

    public boolean delete(int equipId) {
        String delMaint = "DELETE FROM equipment_maintenance WHERE equip_id = ?";
        executeUpdate(delMaint, new Object[]{equipId});
        String sql = "DELETE FROM equipment WHERE equip_id = ?";
        return executeUpdate(sql, new Object[]{equipId}) > 0;
    }

    // === 统计查询 ===

    public int getUsageCount(int equipId) {
        String sql = "SELECT COUNT(*) FROM obs_equipment WHERE equip_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, equipId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("查询器材使用次数失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    public Map<Integer, Integer> batchGetUsageCounts(List<Integer> equipIds) {
        if (equipIds == null || equipIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = equipIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT equip_id, COUNT(*) AS cnt FROM obs_equipment "
                   + "WHERE equip_id IN (" + placeholders + ") GROUP BY equip_id";
        Map<Integer, Integer> result = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < equipIds.size(); i++) {
                stmt.setInt(i + 1, equipIds.get(i));
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getInt("equip_id"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("批量查询使用次数失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return result;
    }

    public List<Equipment> findAllSortedByUsage(int userId) {
        String sql = "SELECT e.equip_id, e.user_id, e.name, e.type, e.aperture, "
                   + "e.focal_length, e.purchase_date, e.status, e.description "
                   + "FROM equipment e "
                   + "LEFT JOIN obs_equipment oe ON e.equip_id = oe.equip_id "
                   + "WHERE e.user_id = ? "
                   + "GROUP BY e.equip_id "
                   + "ORDER BY COUNT(oe.obs_id) DESC";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    // === 搜索 ===

    public List<Equipment> searchByName(int userId, String keyword) {
        String sql = "SELECT equip_id, user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description "
                   + "FROM equipment WHERE user_id = ? AND name LIKE ? "
                   + "ORDER BY name";
        return executeQuery(sql, new Object[]{userId, "%" + keyword + "%"}, this::mapRow);
    }

    // ResultSet 映射
    Equipment mapRow(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipId(rs.getInt("equip_id"));
        e.setUserId(rs.getInt("user_id"));
        e.setName(rs.getString("name"));
        e.setType(EquipType.fromString(rs.getString("type")));
        BigDecimal ap = rs.getBigDecimal("aperture");
        e.setAperture(rs.wasNull() ? null : ap);
        e.setFocalLength(rs.getInt("focal_length"));
        Date pd = rs.getDate("purchase_date");
        e.setPurchaseDate(pd != null ? pd.toLocalDate() : null);
        e.setStatus(EquipStatus.fromString(rs.getString("status")));
        e.setDescription(rs.getString("description"));
        return e;
    }
}
