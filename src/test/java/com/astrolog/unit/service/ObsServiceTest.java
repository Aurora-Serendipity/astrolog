package com.astrolog.unit.service;

import com.astrolog.dao.ObsDao;
import com.astrolog.dao.TagDao;
import com.astrolog.model.Observation;
import com.astrolog.model.enums.MoonPhase;
import com.astrolog.service.ObsService;
import com.astrolog.service.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObsServiceTest {

    @Mock
    private ObsDao obsDao;

    @Mock
    private TagDao tagDao;

    @InjectMocks
    private ObsService obsService;

    private Observation sampleObs;

    @BeforeEach
    void setUp() {
        sampleObs = new Observation();
        sampleObs.setObsId(1);
        sampleObs.setUserId(1);
        sampleObs.setBodyId(10);
        sampleObs.setSiteId(null);
        sampleObs.setObsTime(LocalDateTime.of(2025, 6, 15, 22, 0));
        sampleObs.setLocationLat(new BigDecimal("39.90"));
        sampleObs.setLocationLon(new BigDecimal("116.40"));
        sampleObs.setWeather("晴");
        sampleObs.setSeeing(3);
        sampleObs.setMoonPhase(MoonPhase.FULL_MOON);
        sampleObs.setNote("测试观测");
    }

    // OS-001
    @Test
    void addObservation_success() {
        when(obsDao.insert(any(Observation.class))).thenReturn(1);

        ServiceResult result = obsService.addObservation(sampleObs,
            Arrays.asList(1, 2), Arrays.asList("行星", "深空"));

        assertTrue(result.isSuccess());
        assertEquals("观测记录已添加", result.getMessage());
        verify(obsDao).insert(any(Observation.class));
        verify(obsDao).linkEquipment(1, 1);
        verify(obsDao).linkEquipment(1, 2);
        verify(tagDao, times(2)).getOrCreate(anyString(), eq("#3366CC"));
        verify(obsDao, times(2)).linkTag(eq(1), anyInt());
    }

    // OS-002
    @Test
    void addObservation_emptyBody() {
        sampleObs.setBodyId(0);
        ServiceResult result = obsService.addObservation(sampleObs, null, null);
        assertFalse(result.isSuccess());
        assertEquals("请选择观测星体", result.getMessage());
        verify(obsDao, never()).insert(any());
    }

    // OS-003
    @Test
    void addObservation_nullTime() {
        sampleObs.setObsTime(null);
        ServiceResult result = obsService.addObservation(sampleObs, null, null);
        assertFalse(result.isSuccess());
        assertEquals("观测时间不能为空", result.getMessage());
        verify(obsDao, never()).insert(any());
    }

    // OS-004
    @Test
    void addObservation_futureTime() {
        sampleObs.setObsTime(LocalDateTime.now().plusDays(1));
        ServiceResult result = obsService.addObservation(sampleObs, null, null);
        assertFalse(result.isSuccess());
        assertEquals("观测时间不能在未来", result.getMessage());
        verify(obsDao, never()).insert(any());
    }

    // OS-005
    @Test
    void addObservation_invalidSeeing() {
        sampleObs.setSeeing(0);
        ServiceResult result = obsService.addObservation(sampleObs, null, null);
        assertFalse(result.isSuccess());
        assertEquals("视宁度必须在 1-5 之间", result.getMessage());

        sampleObs.setSeeing(6);
        result = obsService.addObservation(sampleObs, null, null);
        assertFalse(result.isSuccess());
        assertEquals("视宁度必须在 1-5 之间", result.getMessage());

        verify(obsDao, never()).insert(any());
    }

    // OS-006
    @Test
    void updateObservation_success() {
        when(obsDao.update(any(Observation.class))).thenReturn(true);

        ServiceResult result = obsService.updateObservation(sampleObs,
            Arrays.asList(3), Arrays.asList("星系"));

        assertTrue(result.isSuccess());
        assertEquals("观测记录已更新", result.getMessage());
        verify(obsDao).unlinkAllEquipment(1);
        verify(obsDao).linkEquipment(1, 3);
        verify(obsDao).unlinkAllTags(1);
        verify(tagDao).getOrCreate("星系", "#3366CC");
    }

    // OS-007
    @Test
    void deleteObservation_ownRecord() {
        when(obsDao.findById(1)).thenReturn(sampleObs);
        when(obsDao.delete(1)).thenReturn(true);

        ServiceResult result = obsService.deleteObservation(1, 1);

        assertTrue(result.isSuccess());
        assertEquals("已删除", result.getMessage());
        verify(obsDao).delete(1);
    }

    // OS-008
    @Test
    void deleteObservation_othersRecord() {
        sampleObs.setUserId(2);
        when(obsDao.findById(1)).thenReturn(sampleObs);

        ServiceResult result = obsService.deleteObservation(1, 1);

        assertFalse(result.isSuccess());
        assertEquals("无权删除他人记录", result.getMessage());
        verify(obsDao, never()).delete(anyInt());
    }

    // OS-009
    @Test
    void addObservation_withNewTag() {
        when(obsDao.insert(any(Observation.class))).thenReturn(1);
        when(tagDao.getOrCreate("新标签", "#3366CC")).thenReturn(100);

        ServiceResult result = obsService.addObservation(sampleObs,
            null, Arrays.asList("新标签"));

        assertTrue(result.isSuccess());
        verify(tagDao).getOrCreate("新标签", "#3366CC");
        verify(obsDao).linkTag(1, 100);
    }

    // OS-010
    @Test
    void search_byTimeRange() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 30, 0, 0);
        when(obsDao.search(eq(1), eq(start), eq(end), isNull(), isNull(),
            isNull(), isNull(), isNull(), isNull()))
            .thenReturn(Arrays.asList(sampleObs));

        List<Observation> results = obsService.search(1, start, end,
            null, null, null, null, null, null);

        assertEquals(1, results.size());
        verify(obsDao).search(1, start, end, null, null, null, null, null, null);
    }

    // OS-011
    @Test
    void search_byBodyId() {
        when(obsDao.search(eq(1), isNull(), isNull(), eq(10), isNull(),
            isNull(), isNull(), isNull(), isNull()))
            .thenReturn(Arrays.asList(sampleObs));

        List<Observation> results = obsService.search(1, null, null,
            10, null, null, null, null, null);

        assertEquals(1, results.size());
        verify(obsDao).search(1, null, null, 10, null, null, null, null, null);
    }

    // OS-012
    @Test
    void search_byKeyword() {
        when(obsDao.search(eq(1), isNull(), isNull(), isNull(), isNull(),
            isNull(), isNull(), isNull(), eq("测试")))
            .thenReturn(Arrays.asList(sampleObs));

        List<Observation> results = obsService.search(1, null, null,
            null, null, null, null, null, "测试");

        assertEquals(1, results.size());
        verify(obsDao).search(1, null, null, null, null, null, null, null, "测试");
    }
}
