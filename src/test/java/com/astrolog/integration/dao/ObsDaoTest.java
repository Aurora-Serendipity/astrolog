package com.astrolog.integration.dao;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.EquipDao;
import com.astrolog.dao.ObsDao;
import com.astrolog.dao.SiteDao;
import com.astrolog.dao.TagDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.Equipment;
import com.astrolog.model.Observation;
import com.astrolog.model.ObservationSite;
import com.astrolog.model.User;
import com.astrolog.model.enums.BodyType;
import com.astrolog.model.enums.EquipStatus;
import com.astrolog.model.enums.EquipType;
import com.astrolog.model.enums.MoonPhase;
import com.astrolog.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObsDaoTest {

    private ObsDao obsDao;
    private BodyDao bodyDao;
    private EquipDao equipDao;
    private SiteDao siteDao;
    private TagDao tagDao;
    private UserDao userDao;

    private int testUserId;
    private int bodyId;
    private int siteId;
    private int equipId;
    private int tagId;
    private int obsId;

    @BeforeEach
    void setUp() {
        obsDao = new ObsDao();
        bodyDao = new BodyDao();
        equipDao = new EquipDao();
        siteDao = new SiteDao();
        tagDao = new TagDao();
        userDao = new UserDao();

        User u = new User();
        u.setUsername("test_obs_user");
        u.setPassword("test123");
        u.setRole(UserRole.OBSERVER);
        u.setDefaultLat(new BigDecimal("39.90"));
        u.setDefaultLon(new BigDecimal("116.40"));
        testUserId = userDao.insert(u);

        CelestialBody body = new CelestialBody();
        body.setName("测试星体");
        body.setType(BodyType.STAR);
        body.setConstellation("猎户座");
        body.setRaH(5);
        body.setRaM(35);
        body.setDecDeg(-5);
        body.setDecMin(23);
        body.setMagnitude(new BigDecimal("0.45"));
        body.setDistanceLy(new BigDecimal("642"));
        bodyId = bodyDao.insert(body);

        ObservationSite site = new ObservationSite();
        site.setUserId(testUserId);
        site.setName("测试地点");
        site.setLatitude(new BigDecimal("40.00"));
        site.setLongitude(new BigDecimal("117.00"));
        site.setAltitude(800);
        site.setBortleScale(3);
        site.setBestTime("秋季");
        siteId = siteDao.insert(site);

        Equipment equip = new Equipment();
        equip.setUserId(testUserId);
        equip.setName("测试器材");
        equip.setType(EquipType.TELESCOPE);
        equip.setStatus(EquipStatus.ACTIVE);
        equipId = equipDao.insert(equip);

        tagId = tagDao.getOrCreate("测试标签", "#FF0000");
    }

    @AfterEach
    void tearDown() {
        if (obsId > 0) {
            obsDao.delete(obsId);
        }
        if (tagId > 0) {
            tagDao.delete(tagId);
        }
        if (equipId > 0) {
            equipDao.delete(equipId);
        }
        if (siteId > 0) {
            siteDao.delete(siteId);
        }
        if (bodyId > 0) {
            bodyDao.delete(bodyId);
        }
        if (testUserId > 0) {
            userDao.delete(testUserId);
        }
    }

    // IT-OD-001
    @Test
    void insert_and_findById_allFieldsMatch() {
        Observation obs = new Observation();
        obs.setUserId(testUserId);
        obs.setBodyId(bodyId);
        obs.setSiteId(siteId);
        obs.setObsTime(LocalDateTime.of(2025, 7, 20, 23, 0));
        obs.setLocationLat(new BigDecimal("39.90"));
        obs.setLocationLon(new BigDecimal("116.40"));
        obs.setWeather("晴");
        obs.setSeeing(4);
        obs.setMoonPhase(MoonPhase.FIRST_QUARTER);
        obs.setNote("集成测试观测");

        obsId = obsDao.insert(obs);
        assertTrue(obsId > 0);

        Observation found = obsDao.findById(obsId);
        assertNotNull(found);
        assertEquals(testUserId, found.getUserId());
        assertEquals(bodyId, found.getBodyId());
        assertEquals(siteId, found.getSiteId());
        assertEquals(LocalDateTime.of(2025, 7, 20, 23, 0), found.getObsTime());
        assertEquals(0, new BigDecimal("39.90").compareTo(found.getLocationLat()));
        assertEquals("晴", found.getWeather());
        assertEquals(4, found.getSeeing());
        assertEquals(MoonPhase.FIRST_QUARTER, found.getMoonPhase());
        assertEquals("集成测试观测", found.getNote());
    }

    // IT-OD-002
    @Test
    void linkEquipment_and_getLinkedEquipmentIds() {
        Observation obs = createMinimalObs();
        obsId = obsDao.insert(obs);

        obsDao.linkEquipment(obsId, equipId);
        List<Integer> ids = obsDao.getLinkedEquipmentIds(obsId);
        assertEquals(1, ids.size());
        assertTrue(ids.contains(equipId));
    }

    // IT-OD-003
    @Test
    void linkTag_and_getLinkedTagIds() {
        Observation obs = createMinimalObs();
        obsId = obsDao.insert(obs);

        obsDao.linkTag(obsId, tagId);
        List<Integer> ids = obsDao.getLinkedTagIds(obsId);
        assertEquals(1, ids.size());
        assertTrue(ids.contains(tagId));
    }

    // IT-OD-004
    @Test
    void update_then_findById_updatedFieldsCorrect() {
        Observation obs = createMinimalObs();
        obs.setWeather("多云");
        obs.setSeeing(2);
        obsId = obsDao.insert(obs);

        Observation toUpdate = obsDao.findById(obsId);
        toUpdate.setWeather("阴");
        toUpdate.setSeeing(5);
        toUpdate.setMoonPhase(MoonPhase.NEW_MOON);
        assertTrue(obsDao.update(toUpdate));

        Observation updated = obsDao.findById(obsId);
        assertEquals("阴", updated.getWeather());
        assertEquals(5, updated.getSeeing());
        assertEquals(MoonPhase.NEW_MOON, updated.getMoonPhase());
    }

    // IT-OD-005
    @Test
    void delete_cleansUpAllThreeTables() {
        Observation obs = createMinimalObs();
        obsId = obsDao.insert(obs);
        obsDao.linkEquipment(obsId, equipId);
        obsDao.linkTag(obsId, tagId);

        assertTrue(obsDao.delete(obsId));
        assertNull(obsDao.findById(obsId));
        assertTrue(obsDao.getLinkedEquipmentIds(obsId).isEmpty());
        assertTrue(obsDao.getLinkedTagIds(obsId).isEmpty());
        obsId = 0;
    }

    // IT-OD-006
    @Test
    void search_byTimeRange_filtersCorrectly() {
        Observation obs1 = createMinimalObs();
        obs1.setObsTime(LocalDateTime.of(2025, 3, 1, 20, 0));
        int id1 = obsDao.insert(obs1);

        Observation obs2 = createMinimalObs();
        obs2.setObsTime(LocalDateTime.of(2025, 6, 1, 20, 0));
        int id2 = obsDao.insert(obs2);

        Observation obs3 = createMinimalObs();
        obs3.setObsTime(LocalDateTime.of(2025, 9, 1, 20, 0));
        int id3 = obsDao.insert(obs3);

        List<Observation> results = obsDao.search(testUserId,
            LocalDateTime.of(2025, 4, 1, 0, 0),
            LocalDateTime.of(2025, 8, 1, 0, 0),
            null, null, null, null, null, null);

        assertEquals(1, results.size());
        assertEquals(id2, results.get(0).getObsId());

        obsDao.delete(id1);
        obsDao.delete(id2);
        obsDao.delete(id3);
    }

    // IT-OD-007
    @Test
    void search_byBodyId_filtersCorrectly() {
        CelestialBody body2 = new CelestialBody();
        body2.setName("另一星体");
        body2.setType(BodyType.STAR);
        body2.setConstellation("仙女座");
        body2.setRaH(0);
        body2.setRaM(42);
        body2.setDecDeg(41);
        body2.setDecMin(16);
        body2.setMagnitude(new BigDecimal("3.44"));
        body2.setDistanceLy(new BigDecimal("2500000"));
        int bodyId2 = bodyDao.insert(body2);

        Observation obs1 = createMinimalObs();
        obs1.setBodyId(bodyId);
        int id1 = obsDao.insert(obs1);

        Observation obs2 = createMinimalObs();
        obs2.setBodyId(bodyId2);
        int id2 = obsDao.insert(obs2);

        List<Observation> results = obsDao.search(testUserId,
            null, null, bodyId, null, null, null, null, null);

        assertEquals(1, results.size());
        assertEquals(id1, results.get(0).getObsId());

        obsDao.delete(id1);
        obsDao.delete(id2);
        bodyDao.delete(bodyId2);
    }

    // IT-OD-008
    @Test
    void search_byKeyword_findsInNoteAndTags() {
        Observation obs = createMinimalObs();
        obs.setNote("木星大红斑观测笔记");
        obsId = obsDao.insert(obs);
        obsDao.linkTag(obsId, tagId);

        List<Observation> results = obsDao.search(testUserId,
            null, null, null, null, null, null, null, "大红斑");
        assertEquals(1, results.size());
        assertEquals(obsId, results.get(0).getObsId());

        results = obsDao.search(testUserId,
            null, null, null, null, null, null, null, "测试标签");
        assertEquals(1, results.size());
        assertEquals(obsId, results.get(0).getObsId());

        results = obsDao.search(testUserId,
            null, null, null, null, null, null, null, "不存在");
        assertEquals(0, results.size());
    }

    private Observation createMinimalObs() {
        Observation obs = new Observation();
        obs.setUserId(testUserId);
        obs.setBodyId(bodyId);
        obs.setObsTime(LocalDateTime.of(2025, 5, 15, 21, 0));
        obs.setLocationLat(new BigDecimal("39.90"));
        obs.setLocationLon(new BigDecimal("116.40"));
        obs.setSeeing(3);
        return obs;
    }
}
