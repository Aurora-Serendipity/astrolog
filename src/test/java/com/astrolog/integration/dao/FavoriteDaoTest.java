package com.astrolog.integration.dao;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.FavoriteDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.User;
import com.astrolog.model.enums.BodyType;
import com.astrolog.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteDaoTest {

    private FavoriteDao favoriteDao;
    private BodyDao bodyDao;
    private UserDao userDao;
    private int testUserId;
    private int testBodyId;
    private int testBodyId2;

    @BeforeEach
    void setUp() {
        favoriteDao = new FavoriteDao();
        bodyDao = new BodyDao();
        userDao = new UserDao();

        User u = new User();
        u.setUsername("test_fav_user");
        u.setPassword("test123");
        u.setRole(UserRole.OBSERVER);
        testUserId = userDao.insert(u);

        CelestialBody b1 = new CelestialBody();
        b1.setName("TEST_FAV_星体1");
        b1.setType(BodyType.STAR);
        b1.setConstellation("猎户座");
        b1.setRaH(5); b1.setRaM(14);
        b1.setDecDeg(-8); b1.setDecMin(12);
        b1.setMagnitude(new BigDecimal("0.18"));
        testBodyId = bodyDao.insert(b1);

        CelestialBody b2 = new CelestialBody();
        b2.setName("TEST_FAV_星体2");
        b2.setType(BodyType.STAR);
        b2.setConstellation("大犬座");
        b2.setRaH(6); b2.setRaM(45);
        b2.setDecDeg(-16); b2.setDecMin(43);
        b2.setMagnitude(new BigDecimal("-1.46"));
        testBodyId2 = bodyDao.insert(b2);
    }

    @AfterEach
    void tearDown() {
        favoriteDao.remove(testUserId, testBodyId);
        favoriteDao.remove(testUserId, testBodyId2);
        if (testBodyId > 0) bodyDao.delete(testBodyId);
        if (testBodyId2 > 0) bodyDao.delete(testBodyId2);
        if (testUserId > 0) userDao.delete(testUserId);
    }

    // IT-FD-001
    @Test
    void add_then_exists_returnsTrue() {
        assertTrue(favoriteDao.add(testUserId, testBodyId));
        assertTrue(favoriteDao.exists(testUserId, testBodyId));

        List<Integer> ids = favoriteDao.findBodyIdsByUser(testUserId);
        assertTrue(ids.contains(testBodyId));
    }

    // IT-FD-002
    @Test
    void add_then_remove_then_exists_returnsFalse() {
        assertTrue(favoriteDao.add(testUserId, testBodyId));
        assertTrue(favoriteDao.exists(testUserId, testBodyId));

        assertTrue(favoriteDao.remove(testUserId, testBodyId));
        assertFalse(favoriteDao.exists(testUserId, testBodyId));
    }

    // IT-FD-003
    @Test
    void addTwo_then_findBodiesByUser_returnsTwo() {
        assertTrue(favoriteDao.add(testUserId, testBodyId));
        assertTrue(favoriteDao.add(testUserId, testBodyId2));

        List<CelestialBody> bodies = favoriteDao.findBodiesByUser(testUserId);
        assertEquals(2, bodies.size());
        assertTrue(bodies.stream().anyMatch(b -> b.getBodyId() == testBodyId));
        assertTrue(bodies.stream().anyMatch(b -> b.getBodyId() == testBodyId2));
    }
}
