package com.astrolog.integration.dao;

import com.astrolog.dao.UserDao;
import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    private UserDao userDao;
    private int testUserId;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        User u = new User();
        u.setUsername("test_user001");
        u.setPassword("hashed_abc123");
        u.setRole(UserRole.OBSERVER);
        u.setCity("北京");
        u.setDefaultLat(BigDecimal.valueOf(39.9));
        u.setDefaultLon(BigDecimal.valueOf(116.4));
        testUserId = userDao.insert(u);
    }

    @AfterEach
    void tearDown() {
        if (testUserId > 0) {
            userDao.delete(testUserId);
        }
    }

    // IT-UD-001
    @Test
    void insert_and_findById() {
        User u = userDao.findById(testUserId);
        assertNotNull(u);
        assertEquals("test_user001", u.getUsername());
        assertEquals("北京", u.getCity());
        assertEquals(0, BigDecimal.valueOf(39.9).compareTo(u.getDefaultLat()));
        assertEquals(0, BigDecimal.valueOf(116.4).compareTo(u.getDefaultLon()));
    }

    // IT-UD-002
    @Test
    void findByUsername_exactMatch() {
        User u = userDao.findByUsername("test_user001");
        assertNotNull(u);
        assertEquals(testUserId, u.getUserId());

        User notFound = userDao.findByUsername("nonexistent_user_xyz");
        assertNull(notFound);
    }

    // IT-UD-003
    @Test
    void insert_returnsAutoIncrementId() {
        assertTrue(testUserId > 0);
    }

    // IT-UD-004
    @Test
    void update_city_lat_lon() {
        User u = userDao.findById(testUserId);
        assertNotNull(u);
        u.setCity("上海");
        u.setDefaultLat(BigDecimal.valueOf(31.2));
        u.setDefaultLon(BigDecimal.valueOf(121.5));
        assertTrue(userDao.update(u));

        User updated = userDao.findById(testUserId);
        assertEquals("上海", updated.getCity());
        assertEquals(0, BigDecimal.valueOf(31.2).compareTo(updated.getDefaultLat()));
        assertEquals(0, BigDecimal.valueOf(121.5).compareTo(updated.getDefaultLon()));
    }

    // IT-UD-005
    @Test
    void delete_and_findById_returnsNull() {
        assertTrue(userDao.delete(testUserId));
        User deleted = userDao.findById(testUserId);
        assertNull(deleted);
        testUserId = 0;
    }
}
