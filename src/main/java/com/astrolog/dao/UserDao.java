package com.astrolog.dao;

import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class UserDao extends BaseDao<User> {

    public User findByUsername(String username) {
        String sql = "SELECT user_id, username, password, role, avatar_path, "
                   + "city, default_lat, default_lon, login_attempts, "
                   + "locked_until, last_login, create_time "
                   + "FROM users WHERE username = ?";
        List<User> results = executeQuery(sql, new Object[]{username}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public User findById(int userId) {
        String sql = "SELECT user_id, username, password, role, avatar_path, "
                   + "city, default_lat, default_lon, login_attempts, "
                   + "locked_until, last_login, create_time "
                   + "FROM users WHERE user_id = ?";
        List<User> results = executeQuery(sql, new Object[]{userId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<User> findAll() {
        String sql = "SELECT user_id, username, password, role, avatar_path, "
                   + "city, default_lat, default_lon, login_attempts, "
                   + "locked_until, last_login, create_time "
                   + "FROM users ORDER BY create_time DESC";
        return executeQuery(sql, null, this::mapRow);
    }

    public int insert(User user) {
        String sql = "INSERT INTO users (username, password, role, city, "
                   + "default_lat, default_lon) VALUES (?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            user.getUsername(), user.getPassword(), user.getRole().name().toLowerCase(),
            user.getCity(), user.getDefaultLat(), user.getDefaultLon()});
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET city=?, default_lat=?, default_lon=?, "
                   + "avatar_path=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{
            user.getCity(), user.getDefaultLat(), user.getDefaultLon(),
            user.getAvatarPath(), user.getUserId()}) > 0;
    }

    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{hashedPassword, userId}) > 0;
    }

    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id=?";
        return executeUpdate(sql, new Object[]{userId}) > 0;
    }

    public boolean updateLoginAttempts(int userId, int attempts) {
        String sql = "UPDATE users SET login_attempts=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{attempts, userId}) > 0;
    }

    public boolean updateLockedUntil(int userId, LocalDateTime lockedUntil) {
        String sql = "UPDATE users SET locked_until=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{
            lockedUntil != null ? Timestamp.valueOf(lockedUntil) : null, userId}) > 0;
    }

    public boolean updateLastLogin(int userId, LocalDateTime time) {
        String sql = "UPDATE users SET last_login=?, login_attempts=0, locked_until=NULL WHERE user_id=?";
        return executeUpdate(sql, new Object[]{Timestamp.valueOf(time), userId}) > 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(UserRole.fromString(rs.getString("role")));
        u.setAvatarPath(rs.getString("avatar_path"));
        u.setCity(rs.getString("city"));
        BigDecimal lat = rs.getBigDecimal("default_lat");
        u.setDefaultLat(lat);
        BigDecimal lon = rs.getBigDecimal("default_lon");
        u.setDefaultLon(lon);
        u.setLoginAttempts(rs.getInt("login_attempts"));
        Timestamp lockedTs = rs.getTimestamp("locked_until");
        u.setLockedUntil(lockedTs != null ? lockedTs.toLocalDateTime() : null);
        Timestamp lastTs = rs.getTimestamp("last_login");
        u.setLastLogin(lastTs != null ? lastTs.toLocalDateTime() : null);
        Timestamp createTs = rs.getTimestamp("create_time");
        u.setCreateTime(createTs != null ? createTs.toLocalDateTime() : null);
        return u;
    }
}
