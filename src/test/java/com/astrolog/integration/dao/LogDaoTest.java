package com.astrolog.integration.dao;

import com.astrolog.dao.LogDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.OperationLog;
import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogDaoTest {

    private LogDao logDao;
    private UserDao userDao;
    private int testUserId1;
    private int testUserId2;

    @BeforeEach
    void setUp() {
        logDao = new LogDao();
        userDao = new UserDao();
        // 创建测试用户以满足外键约束
        User u1 = new User();
        u1.setUsername("test_loguser1");
        u1.setPassword("hashed_pwd");
        u1.setRole(UserRole.OBSERVER);
        testUserId1 = userDao.insert(u1);

        User u2 = new User();
        u2.setUsername("test_loguser2");
        u2.setPassword("hashed_pwd");
        u2.setRole(UserRole.OBSERVER);
        testUserId2 = userDao.insert(u2);
    }

    @AfterEach
    void tearDown() {
        if (testUserId1 > 0) userDao.delete(testUserId1);
        if (testUserId2 > 0) userDao.delete(testUserId2);
    }

    // IT-LD-001
    @Test
    void insert_and_findByUserId() {
        OperationLog log = new OperationLog();
        log.setUserId(testUserId1);
        log.setOperation("测试操作");
        log.setDetail("测试日志内容");
        log.setIpAddress("127.0.0.1");
        log.setCreateTime(LocalDateTime.now());

        int logId = logDao.insert(log);
        assertTrue(logId > 0);

        List<OperationLog> logs = logDao.findByUserId(testUserId1);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> l.getLogId() == logId));
    }

    // IT-LD-002
    @Test
    void findByUserId_filtersCorrectly() {
        OperationLog log1 = new OperationLog();
        log1.setUserId(testUserId1);
        log1.setOperation("用户1操作");
        log1.setDetail("详情1");
        log1.setIpAddress("127.0.0.1");
        log1.setCreateTime(LocalDateTime.now());
        logDao.insert(log1);

        // 查询 testUserId1 的日志，不应包含 testUserId2 的
        List<OperationLog> logs = logDao.findByUserId(testUserId1);
        for (OperationLog l : logs) {
            assertEquals(testUserId1, l.getUserId());
        }
    }

    // IT-LD-003
    @Test
    void findByTimeRange_filtersCorrectly() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        OperationLog log = new OperationLog();
        log.setUserId(testUserId1);
        log.setOperation("时间测试");
        log.setDetail("时间范围测试");
        log.setIpAddress("127.0.0.1");
        log.setCreateTime(LocalDateTime.now());
        logDao.insert(log);

        List<OperationLog> logs = logDao.findByTimeRange(start, end);
        assertFalse(logs.isEmpty());
        for (OperationLog l : logs) {
            assertFalse(l.getCreateTime().isBefore(start));
            assertFalse(l.getCreateTime().isAfter(end));
        }
    }
}
