package com.astrolog.dao;

import com.astrolog.util.DBUtil;
import com.astrolog.util.RowMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseDao<T> {

    private static final Set<String> ALLOWED_TABLES = Set.of(
        "users", "celestial_bodies", "observations", "equipment",
        "obs_equipment", "observation_sites", "equipment_maintenance",
        "observation_tags", "obs_tag_relation", "user_favorites", "operation_logs"
    );

    protected Connection getConnection() throws SQLException {
        return DBUtil.getInstance().getConnection();
    }

    protected void closeResources(ResultSet rs, Statement stmt, Connection conn) {
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
            DBUtil.getInstance().releaseConnection(conn);
        }
    }

    protected List<T> executeQuery(String sql, Object[] params, RowMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapper.mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("执行查询失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return results;
    }

    protected int executeUpdate(String sql, Object[] params) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("执行更新失败: " + e.getMessage());
            return -1;
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    protected int executeInsert(String sql, Object[] params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("执行插入失败: " + e.getMessage());
            return -1;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    protected boolean dataExists(String tableName) {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("非法的表名: " + tableName);
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = buildExistsQuery(tableName);
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

    private String buildExistsQuery(String tableName) {
        return "SELECT COUNT(*) FROM " + tableName;
    }
}
