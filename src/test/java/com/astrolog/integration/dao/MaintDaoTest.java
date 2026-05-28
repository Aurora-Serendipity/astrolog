package com.astrolog.integration.dao;

import com.astrolog.dao.EquipDao;
import com.astrolog.dao.MaintDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.Equipment;
import com.astrolog.model.EquipmentMaintenance;
import com.astrolog.model.User;
import com.astrolog.model.enums.EquipStatus;
import com.astrolog.model.enums.EquipType;
import com.astrolog.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintDaoTest {

    private MaintDao maintDao;
    private EquipDao equipDao;
    private int testUserId;
    private int testEquipId;
    private int maintId1;
    private int maintId2;

    @BeforeEach
    void setUp() {
        maintDao = new MaintDao();
        equipDao = new EquipDao();

        UserDao userDao = new UserDao();
        User u = new User();
        u.setUsername("test_maint_user");
        u.setPassword("test123");
        u.setRole(UserRole.OBSERVER);
        testUserId = userDao.insert(u);

        Equipment e = new Equipment();
        e.setUserId(testUserId);
        e.setName("测试望远镜");
        e.setType(EquipType.TELESCOPE);
        e.setStatus(EquipStatus.ACTIVE);
        testEquipId = equipDao.insert(e);

        // 维护记录 1: 过去记录
        EquipmentMaintenance m1 = new EquipmentMaintenance();
        m1.setEquipId(testEquipId);
        m1.setMaintDate(LocalDate.of(2025, 6, 1));
        m1.setDescription("镜片清洁");
        m1.setCost(new BigDecimal("300.00"));
        m1.setNextMaintDate(LocalDate.of(2025, 12, 1));
        maintId1 = maintDao.insert(m1);

        // 维护记录 2: 近期维护（下次维护在 15 天内 → 应出现在即将到期列表中）
        EquipmentMaintenance m2 = new EquipmentMaintenance();
        m2.setEquipId(testEquipId);
        m2.setMaintDate(LocalDate.of(2025, 12, 1));
        m2.setDescription("校准");
        m2.setCost(new BigDecimal("500.00"));
        m2.setNextMaintDate(LocalDate.now().plusDays(15));
        maintId2 = maintDao.insert(m2);
    }

    @AfterEach
    void tearDown() {
        if (maintId1 > 0) maintDao.delete(maintId1);
        if (maintId2 > 0) maintDao.delete(maintId2);
        if (testEquipId > 0) equipDao.delete(testEquipId);
        if (testUserId > 0) new UserDao().delete(testUserId);
    }

    // IT-MD-001
    @Test
    void insert_and_findByEquipId() {
        List<EquipmentMaintenance> list = maintDao.findByEquipId(testEquipId);
        assertEquals(2, list.size());
        // 按日期降序，最近的在前面
        assertTrue(list.get(0).getMaintDate().isAfter(list.get(1).getMaintDate())
                || list.get(0).getMaintDate().isEqual(list.get(1).getMaintDate()));
    }

    // IT-MD-002
    @Test
    void update_then_findByEquipId() {
        EquipmentMaintenance m = maintDao.findByEquipId(testEquipId).get(0);
        m.setDescription("镜片深度清洁");
        m.setCost(new BigDecimal("450.00"));
        assertTrue(maintDao.update(m));

        List<EquipmentMaintenance> list = maintDao.findByEquipId(testEquipId);
        EquipmentMaintenance updated = list.stream()
            .filter(x -> x.getMaintId() == m.getMaintId())
            .findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("镜片深度清洁", updated.getDescription());
        assertEquals(0, new BigDecimal("450.00").compareTo(updated.getCost()));
    }

    // IT-MD-003
    @Test
    void delete_then_findByEquipId_empty() {
        assertTrue(maintDao.delete(maintId1));
        maintId1 = 0;
        List<EquipmentMaintenance> list = maintDao.findByEquipId(testEquipId);
        assertEquals(1, list.size());
    }

    // IT-MD-004
    @Test
    void findUpcoming_returnsOnlyWithin30Days() {
        List<EquipmentMaintenance> list = maintDao.findUpcoming(testUserId);
        // 只有 maintId2 的下次维护在 15 天内，maintId1 的下次维护早已过去
        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(m -> m.getNextMaintDate() != null
            && !m.getNextMaintDate().isBefore(LocalDate.now())
            && !m.getNextMaintDate().isAfter(LocalDate.now().plusDays(30))));
    }
}
