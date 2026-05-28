package com.astrolog.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

class DBUtilTest {

    @Test
    void testGetInstance() {
        DBUtil instance = DBUtil.getInstance();
        assertNotNull(instance, "DBUtil 单例不应为 null");
    }

    @Test
    void testGetConnection() throws SQLException {
        Connection conn = DBUtil.getInstance().getConnection();
        assertNotNull(conn, "获取连接不应为 null");
        assertFalse(conn.isClosed(), "连接应处于打开状态");
        DBUtil.getInstance().releaseConnection(conn);
    }

    @Test
    void testReleaseConnection() throws SQLException {
        int idleBefore = DBUtil.getInstance().getIdleCount();
        Connection conn = DBUtil.getInstance().getConnection();
        DBUtil.getInstance().releaseConnection(conn);
        int idleAfter = DBUtil.getInstance().getIdleCount();
        assertEquals(idleBefore, idleAfter, "释放连接后空闲连接数应恢复");
    }
}
