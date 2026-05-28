package com.astrolog.dao;

import com.astrolog.model.OperationLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class LogDao extends BaseDao<OperationLog> {

    public int insert(OperationLog log) {
        String sql = "INSERT INTO operation_logs (user_id, operation, detail, ip_address) "
                   + "VALUES (?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            log.getUserId(), log.getOperation(), log.getDetail(), log.getIpAddress()});
    }

    public List<OperationLog> findByUserId(int userId) {
        String sql = "SELECT log_id, user_id, operation, detail, ip_address, create_time "
                   + "FROM operation_logs WHERE user_id = ? ORDER BY create_time DESC";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public List<OperationLog> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT log_id, user_id, operation, detail, ip_address, create_time "
                   + "FROM operation_logs WHERE create_time BETWEEN ? AND ? "
                   + "ORDER BY create_time DESC";
        return executeQuery(sql, new Object[]{
            Timestamp.valueOf(start), Timestamp.valueOf(end)}, this::mapRow);
    }

    private OperationLog mapRow(ResultSet rs) throws SQLException {
        OperationLog log = new OperationLog();
        log.setLogId(rs.getInt("log_id"));
        log.setUserId(rs.getInt("user_id"));
        log.setOperation(rs.getString("operation"));
        log.setDetail(rs.getString("detail"));
        log.setIpAddress(rs.getString("ip_address"));
        Timestamp ts = rs.getTimestamp("create_time");
        log.setCreateTime(ts != null ? ts.toLocalDateTime() : null);
        return log;
    }
}
