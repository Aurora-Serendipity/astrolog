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

class BodyDaoTest {

    private BodyDao bodyDao;
    private FavoriteDao favoriteDao;
    private int testBodyId;
    private int testBodyId2;
    private int testUserId;

    @BeforeEach
    void setUp() {
        bodyDao = new BodyDao();
        favoriteDao = new FavoriteDao();

        // 创建测试用户用于收藏测试
        UserDao userDao = new UserDao();
        User u = new User();
        u.setUsername("test_body_user");
        u.setPassword("test123");
        u.setRole(UserRole.OBSERVER);
        testUserId = userDao.insert(u);

        // 创建测试星体
        CelestialBody body = new CelestialBody();
        body.setName("TEST_M31 仙女座星系");
        body.setType(BodyType.GALAXY);
        body.setConstellation("仙女座");
        body.setRaH(0); body.setRaM(42);
        body.setDecDeg(41); body.setDecMin(16);
        body.setMagnitude(new BigDecimal("3.44"));
        body.setDistanceLy(new BigDecimal("2537000"));
        body.setMessierNumber(31);
        body.setBestSeason("秋");
        body.setDescription("测试用仙女座星系");
        testBodyId = bodyDao.insert(body);

        CelestialBody body2 = new CelestialBody();
        body2.setName("TEST_M42 猎户座大星云");
        body2.setType(BodyType.NEBULA);
        body2.setConstellation("猎户座");
        body2.setRaH(5); body2.setRaM(35);
        body2.setDecDeg(-5); body2.setDecMin(23);
        body2.setMagnitude(new BigDecimal("4.0"));
        body2.setBestSeason("冬");
        testBodyId2 = bodyDao.insert(body2);
    }

    @AfterEach
    void tearDown() {
        // 清理收藏
        favoriteDao.remove(testUserId, testBodyId);
        favoriteDao.remove(testUserId, testBodyId2);
        // 清理星体
        if (testBodyId > 0) bodyDao.delete(testBodyId);
        if (testBodyId2 > 0) bodyDao.delete(testBodyId2);
        // 清理用户
        if (testUserId > 0) new UserDao().delete(testUserId);
    }

    // IT-BD-001
    @Test
    void insert_and_findById() {
        CelestialBody body = bodyDao.findById(testBodyId);
        assertNotNull(body);
        assertEquals("TEST_M31 仙女座星系", body.getName());
        assertEquals(BodyType.GALAXY, body.getType());
        assertEquals("仙女座", body.getConstellation());
        assertEquals(0, body.getRaH());
        assertEquals(42, body.getRaM());
        assertEquals(41, body.getDecDeg());
        assertEquals(16, body.getDecMin());
        assertEquals(0, new BigDecimal("3.44").compareTo(body.getMagnitude()));
        assertEquals(0, new BigDecimal("2537000").compareTo(body.getDistanceLy()));
        assertEquals(31, body.getMessierNumber());
        assertEquals("秋", body.getBestSeason());
        assertNull(body.getNgcNumber());
    }

    // IT-BD-002
    @Test
    void update_then_findById() {
        CelestialBody body = bodyDao.findById(testBodyId);
        body.setName("TEST_M31 更新");
        body.setMagnitude(new BigDecimal("3.50"));
        assertTrue(bodyDao.update(body));

        CelestialBody updated = bodyDao.findById(testBodyId);
        assertEquals("TEST_M31 更新", updated.getName());
        assertEquals(0, new BigDecimal("3.50").compareTo(updated.getMagnitude()));
    }

    // IT-BD-003
    @Test
    void delete_then_findById_returnsNull() {
        assertTrue(bodyDao.delete(testBodyId));
        CelestialBody deleted = bodyDao.findById(testBodyId);
        assertNull(deleted);
        testBodyId = 0;
    }

    // IT-BD-004
    @Test
    void search_byConstellation() {
        List<CelestialBody> results = bodyDao.search("仙女座", null, null, null, null, null);
        assertTrue(results.stream().allMatch(b -> "仙女座".equals(b.getConstellation())));
        assertTrue(results.stream().anyMatch(b -> b.getName().contains("M31")));
    }

    // IT-BD-005
    @Test
    void search_byType() {
        List<CelestialBody> results = bodyDao.search(null, "nebula", null, null, null, null);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(b -> b.getType() == BodyType.NEBULA));
    }

    // IT-BD-006
    @Test
    void search_byMagnitudeRange() {
        List<CelestialBody> results = bodyDao.search(null, null,
            new BigDecimal("3.0"), new BigDecimal("5.0"), null, null);
        assertFalse(results.isEmpty());
        for (CelestialBody b : results) {
            assertTrue(b.getMagnitude().compareTo(new BigDecimal("3.0")) >= 0);
            assertTrue(b.getMagnitude().compareTo(new BigDecimal("5.0")) <= 0);
        }
    }

    // IT-BD-007
    @Test
    void search_byKeyword() {
        List<CelestialBody> results = bodyDao.search(null, null, null, null, null, "M31");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(b -> b.getName().contains("M31")));
    }

    // IT-BD-008
    @Test
    void delete_withFavorites_cleansUp() {
        favoriteDao.add(testUserId, testBodyId);
        assertTrue(favoriteDao.exists(testUserId, testBodyId));

        assertTrue(bodyDao.delete(testBodyId));
        assertFalse(favoriteDao.exists(testUserId, testBodyId));
        testBodyId = 0;
    }
}
