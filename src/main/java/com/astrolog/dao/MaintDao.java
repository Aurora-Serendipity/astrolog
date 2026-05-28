package com.astrolog.dao;

import com.astrolog.model.EquipmentMaintenance;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class MaintDao extends BaseDao<EquipmentMaintenance> {

    public List<EquipmentMaintenance> findByEquipId(int equipId) {
        String sql = "SELECT maint_id, equip_id, maint_date, description, "
                   + "cost, next_maint_date "
                   + "FROM equipment_maintenance WHERE equip_id = ? "
                   + "ORDER BY maint_date DESC";
        return executeQuery(sql, new Object[]{equipId}, this::mapRow);
    }

    public int insert(EquipmentMaintenance m) {
        String sql = "INSERT INTO equipment_maintenance "
                   + "(equip_id, maint_date, description, cost, next_maint_date) "
                   + "VALUES (?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            m.getEquipId(),
            Date.valueOf(m.getMaintDate()),
            m.getDescription(),
            m.getCost(),
            m.getNextMaintDate() != null
                ? Date.valueOf(m.getNextMaintDate()) : null});
    }

    public boolean update(EquipmentMaintenance m) {
        String sql = "UPDATE equipment_maintenance SET maint_date=?, "
                   + "description=?, cost=?, next_maint_date=? "
                   + "WHERE maint_id=?";
        return executeUpdate(sql, new Object[]{
            Date.valueOf(m.getMaintDate()),
            m.getDescription(),
            m.getCost(),
            m.getNextMaintDate() != null
                ? Date.valueOf(m.getNextMaintDate()) : null,
            m.getMaintId()}) > 0;
    }

    public boolean delete(int maintId) {
        String sql = "DELETE FROM equipment_maintenance WHERE maint_id = ?";
        return executeUpdate(sql, new Object[]{maintId}) > 0;
    }

    public List<EquipmentMaintenance> findUpcoming(int userId) {
        String sql = "SELECT em.maint_id, em.equip_id, em.maint_date, "
                   + "em.description, em.cost, em.next_maint_date "
                   + "FROM equipment_maintenance em "
                   + "JOIN equipment e ON em.equip_id = e.equip_id "
                   + "WHERE e.user_id = ? "
                   + "AND em.next_maint_date BETWEEN CURDATE() "
                   + "AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) "
                   + "ORDER BY em.next_maint_date";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    private EquipmentMaintenance mapRow(ResultSet rs) throws SQLException {
        EquipmentMaintenance m = new EquipmentMaintenance();
        m.setMaintId(rs.getInt("maint_id"));
        m.setEquipId(rs.getInt("equip_id"));
        Date md = rs.getDate("maint_date");
        m.setMaintDate(md != null ? md.toLocalDate() : null);
        m.setDescription(rs.getString("description"));
        m.setCost(rs.getBigDecimal("cost"));
        Date nd = rs.getDate("next_maint_date");
        m.setNextMaintDate(nd != null ? nd.toLocalDate() : null);
        return m;
    }
}
