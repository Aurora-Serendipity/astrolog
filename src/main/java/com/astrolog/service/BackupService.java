package com.astrolog.service;

import com.astrolog.util.DBUtil;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private static final String[] TABLES = {
        "users", "celestial_bodies", "observation_sites",
        "equipment", "observations", "obs_equipment",
        "equipment_maintenance", "observation_tags",
        "obs_tag_relation", "user_favorites", "operation_logs"
    };

    // FK-safe truncation order: child tables first
    private static final String[] TRUNCATE_ORDER = {
        "obs_equipment", "equipment_maintenance", "obs_tag_relation",
        "user_favorites", "operation_logs", "observations",
        "observation_tags", "equipment", "observation_sites",
        "celestial_bodies", "users"
    };

    public BackupService() {}

    public String backup(String outputDir) throws Exception {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path file = Paths.get(outputDir, "astrolog_backup_" + timestamp + ".sql");
        Connection conn = DBUtil.getInstance().getConnection();

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("-- AstroLog 数据备份 (" + timestamp + ")\n\n");
            writer.write("USE astrolog;\n\n");

            for (String table : TABLES) {
                writer.write("-- 表: " + table + "\n");

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder("INSERT INTO ")
                            .append(table).append(" VALUES (");
                        for (int i = 1; i <= colCount; i++) {
                            if (i > 1) sb.append(", ");
                            String val = rs.getString(i);
                            if (val == null) {
                                sb.append("NULL");
                            } else if (isNumericType(meta.getColumnTypeName(i))) {
                                sb.append(val);
                            } else if (isTemporalType(meta.getColumnTypeName(i))) {
                                sb.append("'").append(val).append("'");
                            } else {
                                sb.append("'").append(val.replace("'", "\\'")).append("'");
                            }
                        }
                        sb.append(");\n");
                        writer.write(sb.toString());
                    }
                }
                writer.write("\n");
            }
        } finally {
            DBUtil.getInstance().releaseConnection(conn);
        }
        return file.toAbsolutePath().toString();
    }

    private boolean isNumericType(String typeName) {
        String upper = typeName.toUpperCase();
        return upper.contains("INT") || upper.contains("DECIMAL")
            || upper.contains("FLOAT") || upper.contains("DOUBLE");
    }

    private boolean isTemporalType(String typeName) {
        String upper = typeName.toUpperCase();
        return upper.contains("DATE") || upper.contains("TIME")
            || upper.contains("YEAR");
    }

    public ServiceResult restore(String backupPath) throws Exception {
        String content = Files.readString(Paths.get(backupPath), StandardCharsets.UTF_8);
        String[] statements = content.split(";\\s*\n");

        Connection conn = DBUtil.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            // Disable FK checks during restore
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Truncate all tables
            for (String table : TRUNCATE_ORDER) {
                stmt.execute("DELETE FROM " + table);
            }

            // Execute backup INSERT statements
            for (String sql : statements) {
                sql = sql.trim();
                if (sql.isEmpty() || sql.startsWith("--")) continue;
                stmt.execute(sql);
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            conn.commit();
            return ServiceResult.success("数据恢复成功");
        } catch (SQLException e) {
            conn.rollback();
            return ServiceResult.fail("恢复失败: " + e.getMessage());
        } finally {
            DBUtil.getInstance().releaseConnection(conn);
        }
    }
}
