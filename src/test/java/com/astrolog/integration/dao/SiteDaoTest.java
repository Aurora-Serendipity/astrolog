package com.astrolog.integration.dao;

import com.astrolog.dao.SiteDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.ObservationSite;
import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SiteDaoTest {

    private SiteDao siteDao;
    private UserDao userDao;
    private int testUserId;
    private int siteId1;
    private int siteId2;
    private int siteId3;

    @BeforeEach
    void setUp() {
        siteDao = new SiteDao();
        userDao = new UserDao();

        User u = new User();
        u.setUsername("test_site_user");
        u.setPassword("test123");
        u.setRole(UserRole.OBSERVER);
        testUserId = userDao.insert(u);
    }

    @AfterEach
    void tearDown() {
        if (siteId1 > 0) siteDao.delete(siteId1);
        if (siteId2 > 0) siteDao.delete(siteId2);
        if (siteId3 > 0) siteDao.delete(siteId3);
        if (testUserId > 0) userDao.delete(testUserId);
    }

    // IT-SD-001
    @Test
    void insert_and_findById_fieldsMatch() {
        ObservationSite site = new ObservationSite();
        site.setUserId(testUserId);
        site.setName("测试观测地A");
        site.setLatitude(new BigDecimal("30.50"));
        site.setLongitude(new BigDecimal("120.10"));
        site.setAltitude(200);
        site.setBortleScale(4);
        site.setBestTime("夏季");

        siteId1 = siteDao.insert(site);
        assertTrue(siteId1 > 0);

        ObservationSite found = siteDao.findById(siteId1);
        assertNotNull(found);
        assertEquals("测试观测地A", found.getName());
        assertEquals(0, new BigDecimal("30.50").compareTo(found.getLatitude()));
        assertEquals(0, new BigDecimal("120.10").compareTo(found.getLongitude()));
        assertEquals(200, found.getAltitude());
        assertEquals(4, found.getBortleScale());
        assertEquals("夏季", found.getBestTime());
        assertEquals(testUserId, found.getUserId());
    }

    // IT-SD-002
    @Test
    void update_then_findById_fieldsUpdated() {
        ObservationSite site = new ObservationSite();
        site.setUserId(testUserId);
        site.setName("测试观测地B");
        site.setLatitude(new BigDecimal("25.00"));
        site.setLongitude(new BigDecimal("110.00"));
        site.setAltitude(100);
        site.setBortleScale(5);
        site.setBestTime("春季");
        siteId1 = siteDao.insert(site);

        ObservationSite toUpdate = siteDao.findById(siteId1);
        toUpdate.setName("测试观测地B-已更新");
        toUpdate.setBortleScale(3);
        assertTrue(siteDao.update(toUpdate));

        ObservationSite updated = siteDao.findById(siteId1);
        assertEquals("测试观测地B-已更新", updated.getName());
        assertEquals(3, updated.getBortleScale());
    }

    // IT-SD-003
    @Test
    void delete_then_findById_returnsNull() {
        ObservationSite site = new ObservationSite();
        site.setUserId(testUserId);
        site.setName("待删除地点");
        site.setLatitude(new BigDecimal("35.00"));
        site.setLongitude(new BigDecimal("115.00"));
        site.setAltitude(50);
        site.setBortleScale(6);
        siteId1 = siteDao.insert(site);

        assertTrue(siteDao.delete(siteId1));
        ObservationSite deleted = siteDao.findById(siteId1);
        assertNull(deleted);
        siteId1 = 0;
    }

    // IT-SD-004
    @Test
    void findAllByUserId_returnsAllSites() {
        ObservationSite s1 = new ObservationSite();
        s1.setUserId(testUserId);
        s1.setName("地点A");
        s1.setLatitude(new BigDecimal("30.00"));
        s1.setLongitude(new BigDecimal("120.00"));
        s1.setAltitude(100);
        s1.setBortleScale(3);
        siteId1 = siteDao.insert(s1);

        ObservationSite s2 = new ObservationSite();
        s2.setUserId(testUserId);
        s2.setName("地点B");
        s2.setLatitude(new BigDecimal("31.00"));
        s2.setLongitude(new BigDecimal("121.00"));
        s2.setAltitude(200);
        s2.setBortleScale(4);
        siteId2 = siteDao.insert(s2);

        List<ObservationSite> list = siteDao.findAllByUserId(testUserId);
        assertEquals(2, list.size());
    }
}
