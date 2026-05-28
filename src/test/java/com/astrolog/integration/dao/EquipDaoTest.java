package com.astrolog.integration.dao;

import com.astrolog.dao.EquipDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.Equipment;
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

class EquipDaoTest {

    private EquipDao equipDao;
    private int testUserId;
    private int equipId1;
    private int equipId2;
    private int equipId3;

    @BeforeEach
    void setUp() {
        equipDao = new EquipDao();

        UserDao userDao = new UserDao();
        User u = new User();
        u.setUsername("test_equip_user");
        u.setPassword("test123");
        u.setRole(UserRole.OBSERVER);
        testUserId = userDao.insert(u);

        // 器材 1
        Equipment e1 = new Equipment();
        e1.setUserId(testUserId);
        e1.setName("星特朗 8SE");
        e1.setType(EquipType.TELESCOPE);
        e1.setAperture(new BigDecimal("203.2"));
        e1.setFocalLength(2032);
        e1.setPurchaseDate(LocalDate.of(2025, 1, 15));
        e1.setStatus(EquipStatus.ACTIVE);
        e1.setDescription("主镜");
        equipId1 = equipDao.insert(e1);

        // 器材 2
        Equipment e2 = new Equipment();
        e2.setUserId(testUserId);
        e2.setName("Tele Vue 13mm");
        e2.setType(EquipType.EYEPIECE);
        e2.setFocalLength(13);
        e2.setStatus(EquipStatus.ACTIVE);
        equipId2 = equipDao.insert(e2);

        // 器材 3
        Equipment e3 = new Equipment();
        e3.setUserId(testUserId);
        e3.setName("ASI224MC");
        e3.setType(EquipType.CAMERA);
        e3.setStatus(EquipStatus.ACTIVE);
        equipId3 = equipDao.insert(e3);
    }

    @AfterEach
    void tearDown() {
        if (equipId1 > 0) equipDao.delete(equipId1);
        if (equipId2 > 0) equipDao.delete(equipId2);
        if (equipId3 > 0) equipDao.delete(equipId3);
        if (testUserId > 0) new UserDao().delete(testUserId);
    }

    // IT-ED-001
    @Test
    void insert_and_findById() {
        Equipment e = equipDao.findById(equipId1);
        assertNotNull(e);
        assertEquals("星特朗 8SE", e.getName());
        assertEquals(EquipType.TELESCOPE, e.getType());
        assertEquals(0, new BigDecimal("203.2").compareTo(e.getAperture()));
        assertEquals(2032, e.getFocalLength());
        assertEquals(LocalDate.of(2025, 1, 15), e.getPurchaseDate());
        assertEquals(EquipStatus.ACTIVE, e.getStatus());
        assertEquals("主镜", e.getDescription());
        assertEquals(testUserId, e.getUserId());
    }

    // IT-ED-002
    @Test
    void update_then_findById() {
        Equipment e = equipDao.findById(equipId1);
        e.setName("星特朗 8SE 升级版");
        e.setStatus(EquipStatus.MAINTENANCE);
        assertTrue(equipDao.update(e));

        Equipment updated = equipDao.findById(equipId1);
        assertEquals("星特朗 8SE 升级版", updated.getName());
        assertEquals(EquipStatus.MAINTENANCE, updated.getStatus());
    }

    // IT-ED-003
    @Test
    void delete_then_findById_returnsNull() {
        assertTrue(equipDao.delete(equipId2));
        Equipment deleted = equipDao.findById(equipId2);
        assertNull(deleted);
        equipId2 = 0;
    }

    // IT-ED-004
    @Test
    void findAllByUserId_returnsAllEquipment() {
        List<Equipment> list = equipDao.findAllByUserId(testUserId);
        assertEquals(3, list.size());
    }

    // IT-ED-005: findById with non-existent ID returns null
    @Test
    void findById_nonExistentId_returnsNull() {
        assertNull(equipDao.findById(99999));
    }

    // IT-ED-006
    @Test
    void searchByName_fuzzyMatch() {
        List<Equipment> results = equipDao.searchByName(testUserId, "星特朗");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.getName().contains("星特朗")));
        assertFalse(results.stream().anyMatch(e -> "Tele Vue 13mm".equals(e.getName())));
    }

    // IT-ED-007
    @Test
    void findAllSortedByUsage() {
        List<Equipment> list = equipDao.findAllSortedByUsage(testUserId);
        assertEquals(3, list.size());
        // 所有器材均无观测记录，排序后列表非空即可验证 JOIN 语法正确
    }
}
