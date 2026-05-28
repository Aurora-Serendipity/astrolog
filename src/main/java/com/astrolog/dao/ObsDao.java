package com.astrolog.dao;

import com.astrolog.model.Observation;
import com.astrolog.model.enums.MoonPhase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ObsDao extends BaseDao<Observation> {

    // === 基础 CRUD ===

    public Observation findById(int obsId) {
        String sql = "SELECT obs_id, user_id, body_id, site_id, obs_time, "
                   + "location_lat, location_lon, weather, seeing, moon_phase, "
                   + "note, create_time FROM observations WHERE obs_id = ?";
        List<Observation> results = executeQuery(sql, new Object[]{obsId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Observation> findAllByUserId(int userId) {
        String sql = "SELECT obs_id, user_id, body_id, site_id, obs_time, "
                   + "location_lat, location_lon, weather, seeing, moon_phase, "
                   + "note, create_time FROM observations "
                   + "WHERE user_id = ? ORDER BY obs_time DESC";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public int insert(Observation obs) {
        String sql = "INSERT INTO observations (user_id, body_id, site_id, obs_time, "
                   + "location_lat, location_lon, weather, seeing, moon_phase, note) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            obs.getUserId(), obs.getBodyId(),
            obs.getSiteId(),
            Timestamp.valueOf(obs.getObsTime()),
            obs.getLocationLat(), obs.getLocationLon(),
            obs.getWeather(), obs.getSeeing(),
            obs.getMoonPhase() != null
                ? obs.getMoonPhase().name().toLowerCase() : null,
            obs.getNote()});
    }

    public boolean update(Observation obs) {
        String sql = "UPDATE observations SET body_id=?, site_id=?, obs_time=?, "
                   + "location_lat=?, location_lon=?, weather=?, seeing=?, "
                   + "moon_phase=?, note=? WHERE obs_id=?";
        return executeUpdate(sql, new Object[]{
            obs.getBodyId(), obs.getSiteId(),
            Timestamp.valueOf(obs.getObsTime()),
            obs.getLocationLat(), obs.getLocationLon(),
            obs.getWeather(), obs.getSeeing(),
            obs.getMoonPhase() != null
                ? obs.getMoonPhase().name().toLowerCase() : null,
            obs.getNote(), obs.getObsId()}) > 0;
    }

    public boolean delete(int obsId) {
        executeUpdate("DELETE FROM obs_equipment WHERE obs_id = ?",
            new Object[]{obsId});
        executeUpdate("DELETE FROM obs_tag_relation WHERE obs_id = ?",
            new Object[]{obsId});
        return executeUpdate("DELETE FROM observations WHERE obs_id = ?",
            new Object[]{obsId}) > 0;
    }

    // === 多对多关联操作 ===

    public void linkEquipment(int obsId, int equipId) {
        String sql = "INSERT IGNORE INTO obs_equipment (obs_id, equip_id) VALUES (?, ?)";
        executeUpdate(sql, new Object[]{obsId, equipId});
    }

    public void unlinkAllEquipment(int obsId) {
        String sql = "DELETE FROM obs_equipment WHERE obs_id = ?";
        executeUpdate(sql, new Object[]{obsId});
    }

    public List<Integer> getLinkedEquipmentIds(int obsId) {
        String sql = "SELECT equip_id FROM obs_equipment WHERE obs_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, obsId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("equip_id"));
            }
        } catch (SQLException e) {
            System.err.println("查询关联器材失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return ids;
    }

    public void linkTag(int obsId, int tagId) {
        String sql = "INSERT IGNORE INTO obs_tag_relation (obs_id, tag_id) VALUES (?, ?)";
        executeUpdate(sql, new Object[]{obsId, tagId});
    }

    public void unlinkAllTags(int obsId) {
        String sql = "DELETE FROM obs_tag_relation WHERE obs_id = ?";
        executeUpdate(sql, new Object[]{obsId});
    }

    public List<Integer> getLinkedTagIds(int obsId) {
        String sql = "SELECT tag_id FROM obs_tag_relation WHERE obs_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, obsId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("tag_id"));
            }
        } catch (SQLException e) {
            System.err.println("查询关联标签失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return ids;
    }

    // === 组合条件查询（动态 SQL） ===

    public List<Observation> search(int userId, LocalDateTime startTime,
                                     LocalDateTime endTime, Integer bodyId,
                                     Integer siteId, String weather,
                                     Integer seeing, String moonPhase,
                                     String keyword) {
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT o.obs_id, o.user_id, o.body_id, o.site_id, "
          + "o.obs_time, o.location_lat, o.location_lon, o.weather, "
          + "o.seeing, o.moon_phase, o.note, o.create_time "
          + "FROM observations o "
          + "LEFT JOIN obs_tag_relation otr ON o.obs_id = otr.obs_id "
          + "LEFT JOIN observation_tags ot ON otr.tag_id = ot.tag_id "
          + "WHERE o.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startTime != null) {
            sql.append(" AND o.obs_time >= ?");
            params.add(Timestamp.valueOf(startTime));
        }
        if (endTime != null) {
            sql.append(" AND o.obs_time <= ?");
            params.add(Timestamp.valueOf(endTime));
        }
        if (bodyId != null) {
            sql.append(" AND o.body_id = ?");
            params.add(bodyId);
        }
        if (siteId != null) {
            sql.append(" AND o.site_id = ?");
            params.add(siteId);
        }
        if (weather != null && !weather.isEmpty()) {
            sql.append(" AND o.weather = ?");
            params.add(weather);
        }
        if (seeing != null) {
            sql.append(" AND o.seeing = ?");
            params.add(seeing);
        }
        if (moonPhase != null && !moonPhase.isEmpty()) {
            sql.append(" AND o.moon_phase = ?");
            params.add(moonPhase.toLowerCase());
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (o.note LIKE ? OR ot.name LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        sql.append(" ORDER BY o.obs_time DESC");
        return executeQuery(sql.toString(), params.toArray(), this::mapRow);
    }

    // === ResultSet 映射 ===

    Observation mapRow(ResultSet rs) throws SQLException {
        Observation o = new Observation();
        o.setObsId(rs.getInt("obs_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setBodyId(rs.getInt("body_id"));
        int sid = rs.getInt("site_id");
        o.setSiteId(rs.wasNull() ? null : sid);
        Timestamp ot = rs.getTimestamp("obs_time");
        o.setObsTime(ot != null ? ot.toLocalDateTime() : null);
        o.setLocationLat(rs.getBigDecimal("location_lat"));
        o.setLocationLon(rs.getBigDecimal("location_lon"));
        o.setWeather(rs.getString("weather"));
        o.setSeeing(rs.getInt("seeing"));
        String mp = rs.getString("moon_phase");
        o.setMoonPhase(mp != null ? MoonPhase.fromString(mp) : null);
        o.setNote(rs.getString("note"));
        Timestamp ct = rs.getTimestamp("create_time");
        o.setCreateTime(ct != null ? ct.toLocalDateTime() : null);
        return o;
    }
}
