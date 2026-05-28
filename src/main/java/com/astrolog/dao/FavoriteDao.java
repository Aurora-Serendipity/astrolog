package com.astrolog.dao;

import com.astrolog.model.CelestialBody;
import com.astrolog.model.UserFavorite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao extends BaseDao<UserFavorite> {

    private final BodyDao bodyDao = new BodyDao();

    public boolean add(int userId, int bodyId) {
        if (exists(userId, bodyId)) return true;
        String sql = "INSERT INTO user_favorites (user_id, body_id) VALUES (?, ?)";
        return executeUpdate(sql, new Object[]{userId, bodyId}) > 0;
    }

    public boolean remove(int userId, int bodyId) {
        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND body_id = ?";
        return executeUpdate(sql, new Object[]{userId, bodyId}) > 0;
    }

    public boolean exists(int userId, int bodyId) {
        String sql = "SELECT COUNT(*) FROM user_favorites WHERE user_id = ? AND body_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bodyId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查收藏失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return false;
    }

    public List<Integer> findBodyIdsByUser(int userId) {
        String sql = "SELECT body_id FROM user_favorites WHERE user_id = ? "
                   + "ORDER BY create_time DESC";
        List<Integer> ids = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("body_id"));
            }
        } catch (SQLException e) {
            System.err.println("查询收藏星体ID失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return ids;
    }

    public List<CelestialBody> findBodiesByUser(int userId) {
        String sql = "SELECT cb.body_id, cb.name, cb.type, cb.constellation, "
                   + "cb.ra_h, cb.ra_m, cb.dec_deg, cb.dec_min, cb.magnitude, "
                   + "cb.distance_ly, cb.messier_number, cb.ngc_number, "
                   + "cb.best_season, cb.description, cb.image_path "
                   + "FROM celestial_bodies cb "
                   + "INNER JOIN user_favorites uf ON cb.body_id = uf.body_id "
                   + "WHERE uf.user_id = ? "
                   + "ORDER BY uf.create_time DESC";
        List<CelestialBody> bodies = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                bodies.add(bodyDao.mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询收藏星体失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return bodies;
    }

    private UserFavorite mapRow(ResultSet rs) throws SQLException {
        UserFavorite f = new UserFavorite();
        f.setUserId(rs.getInt("user_id"));
        f.setBodyId(rs.getInt("body_id"));
        Timestamp ts = rs.getTimestamp("create_time");
        f.setCreateTime(ts != null ? ts.toLocalDateTime() : null);
        return f;
    }
}
