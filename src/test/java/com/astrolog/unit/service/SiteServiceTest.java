package com.astrolog.unit.service;

import com.astrolog.dao.SiteDao;
import com.astrolog.model.ObservationSite;
import com.astrolog.service.ServiceResult;
import com.astrolog.service.SiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteServiceTest {

    @Mock
    private SiteDao siteDao;

    @InjectMocks
    private SiteService siteService;

    private ObservationSite sampleSite;

    @BeforeEach
    void setUp() {
        sampleSite = new ObservationSite();
        sampleSite.setSiteId(1);
        sampleSite.setUserId(1);
        sampleSite.setName("测试观测地");
        sampleSite.setLatitude(new BigDecimal("39.90"));
        sampleSite.setLongitude(new BigDecimal("116.40"));
        sampleSite.setAltitude(50);
        sampleSite.setBortleScale(4);
        sampleSite.setBestTime("夏季");
    }

    // SS-001
    @Test
    void addSite_success() {
        when(siteDao.insert(any(ObservationSite.class))).thenReturn(1);

        ServiceResult result = siteService.addSite(sampleSite);

        assertTrue(result.isSuccess());
        assertEquals("地点已添加", result.getMessage());
        verify(siteDao).insert(sampleSite);
    }

    // SS-002
    @Test
    void addSite_emptyName() {
        sampleSite.setName("");
        ServiceResult result = siteService.addSite(sampleSite);
        assertFalse(result.isSuccess());
        assertEquals("地点名称不能为空", result.getMessage());
        verify(siteDao, never()).insert(any());
    }

    // SS-003
    @Test
    void addSite_invalidCoordinates() {
        sampleSite.setLatitude(new BigDecimal("91.00"));
        ServiceResult result = siteService.addSite(sampleSite);
        assertFalse(result.isSuccess());
        assertEquals("纬度必须在 -90 到 90 之间", result.getMessage());

        sampleSite.setLatitude(new BigDecimal("39.90"));
        sampleSite.setLongitude(new BigDecimal("181.00"));
        result = siteService.addSite(sampleSite);
        assertFalse(result.isSuccess());
        assertEquals("经度必须在 -180 到 180 之间", result.getMessage());
    }

    // SS-004
    @Test
    void addSite_invalidBortle() {
        sampleSite.setBortleScale(0);
        ServiceResult result = siteService.addSite(sampleSite);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("波特尔"));

        sampleSite.setBortleScale(10);
        result = siteService.addSite(sampleSite);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("波特尔"));
    }
}
