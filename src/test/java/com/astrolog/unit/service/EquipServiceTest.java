package com.astrolog.unit.service;

import com.astrolog.dao.EquipDao;
import com.astrolog.dao.MaintDao;
import com.astrolog.model.Equipment;
import com.astrolog.model.EquipmentMaintenance;
import com.astrolog.model.enums.EquipStatus;
import com.astrolog.model.enums.EquipType;
import com.astrolog.service.EquipService;
import com.astrolog.service.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipServiceTest {

    @Mock
    private EquipDao equipDao;

    @Mock
    private MaintDao maintDao;

    @InjectMocks
    private EquipService equipService;

    private Equipment sampleEquip;
    private EquipmentMaintenance sampleMaint;

    @BeforeEach
    void setUp() {
        sampleEquip = new Equipment();
        sampleEquip.setEquipId(1);
        sampleEquip.setUserId(1);
        sampleEquip.setName("星特朗 8SE");
        sampleEquip.setType(EquipType.TELESCOPE);
        sampleEquip.setAperture(new BigDecimal("203.2"));
        sampleEquip.setFocalLength(2032);
        sampleEquip.setPurchaseDate(LocalDate.of(2025, 1, 15));
        sampleEquip.setStatus(EquipStatus.ACTIVE);

        sampleMaint = new EquipmentMaintenance();
        sampleMaint.setMaintId(1);
        sampleMaint.setEquipId(1);
        sampleMaint.setMaintDate(LocalDate.of(2025, 6, 1));
        sampleMaint.setDescription("镜片清洁");
        sampleMaint.setCost(new BigDecimal("300.00"));
        sampleMaint.setNextMaintDate(LocalDate.of(2026, 6, 1));
    }

    // ES-001
    @Test
    void addEquipment_success() {
        when(equipDao.insert(any(Equipment.class))).thenReturn(1);
        ServiceResult result = equipService.addEquipment(sampleEquip);
        assertTrue(result.isSuccess());
        assertEquals("器材已添加", result.getMessage());
        verify(equipDao).insert(sampleEquip);
    }

    // ES-002
    @Test
    void addEquipment_emptyName() {
        sampleEquip.setName("");
        ServiceResult result = equipService.addEquipment(sampleEquip);
        assertFalse(result.isSuccess());
        assertEquals("器材名称不能为空", result.getMessage());
        verify(equipDao, never()).insert(any());
    }

    // ES-003
    @Test
    void addEquipment_nullType() {
        sampleEquip.setType(null);
        ServiceResult result = equipService.addEquipment(sampleEquip);
        assertFalse(result.isSuccess());
        assertEquals("器材类型不能为空", result.getMessage());
        verify(equipDao, never()).insert(any());
    }

    // ES-004
    @Test
    void updateEquipment_success() {
        when(equipDao.update(any(Equipment.class))).thenReturn(true);
        ServiceResult result = equipService.updateEquipment(sampleEquip);
        assertTrue(result.isSuccess());
        assertEquals("器材信息已更新", result.getMessage());
        verify(equipDao).update(sampleEquip);
    }

    // ES-005
    @Test
    void deleteEquipment_success() {
        when(equipDao.delete(1)).thenReturn(true);
        ServiceResult result = equipService.deleteEquipment(1);
        assertTrue(result.isSuccess());
        assertEquals("器材已删除", result.getMessage());
        verify(equipDao).delete(1);
    }

    // ES-006
    @Test
    void addMaintenance_success() {
        when(maintDao.insert(any(EquipmentMaintenance.class))).thenReturn(1);
        ServiceResult result = equipService.addMaintenance(sampleMaint);
        assertTrue(result.isSuccess());
        assertEquals("维护记录已添加", result.getMessage());
        verify(maintDao).insert(sampleMaint);
    }

    // ES-007
    @Test
    void addMaintenance_invalidEquip() {
        sampleMaint.setEquipId(0);
        ServiceResult result = equipService.addMaintenance(sampleMaint);
        assertFalse(result.isSuccess());
        assertEquals("器材 ID 无效", result.getMessage());
        verify(maintDao, never()).insert(any());
    }

    // ES-008
    @Test
    void addMaintenance_nullDate() {
        sampleMaint.setMaintDate(null);
        ServiceResult result = equipService.addMaintenance(sampleMaint);
        assertFalse(result.isSuccess());
        assertEquals("维护日期不能为空", result.getMessage());
        verify(maintDao, never()).insert(any());
    }

    // ES-009
    @Test
    void getUsageCount() {
        when(equipDao.getUsageCount(1)).thenReturn(5);
        int count = equipService.getUsageCount(1);
        assertEquals(5, count);
        verify(equipDao).getUsageCount(1);
    }

    // ES-010
    @Test
    void getUpcomingMaintenance() {
        List<EquipmentMaintenance> maintList = Collections.singletonList(sampleMaint);
        when(maintDao.findUpcoming(1)).thenReturn(maintList);
        List<EquipmentMaintenance> result = equipService.getUpcomingMaintenance(1);
        assertEquals(1, result.size());
        assertEquals(sampleMaint.getDescription(), result.get(0).getDescription());
        verify(maintDao).findUpcoming(1);
    }
}
