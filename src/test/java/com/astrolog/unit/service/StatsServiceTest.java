package com.astrolog.unit.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.EquipDao;
import com.astrolog.dao.ObsDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.Equipment;
import com.astrolog.model.Observation;
import com.astrolog.model.enums.BodyType;
import com.astrolog.model.enums.MoonPhase;
import com.astrolog.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ObsDao obsDao;

    @Mock
    private BodyDao bodyDao;

    @Mock
    private EquipDao equipDao;

    @InjectMocks
    private StatsService statsService;

    private Observation obs1;
    private Observation obs2;
    private Observation obs3;
    private CelestialBody bodyStar;
    private CelestialBody bodyPlanet;

    @BeforeEach
    void setUp() {
        obs1 = new Observation();
        obs1.setObsId(1);
        obs1.setUserId(1);
        obs1.setBodyId(10);
        obs1.setObsTime(LocalDateTime.of(2024, 3, 15, 22, 0));
        obs1.setWeather("晴");
        obs1.setSeeing(4);
        obs1.setMoonPhase(MoonPhase.FULL_MOON);

        obs2 = new Observation();
        obs2.setObsId(2);
        obs2.setUserId(1);
        obs2.setBodyId(10);
        obs2.setObsTime(LocalDateTime.of(2024, 6, 20, 21, 0));
        obs2.setWeather("晴");
        obs2.setSeeing(3);
        obs2.setMoonPhase(MoonPhase.NEW_MOON);

        obs3 = new Observation();
        obs3.setObsId(3);
        obs3.setUserId(1);
        obs3.setBodyId(20);
        obs3.setObsTime(LocalDateTime.of(2025, 1, 10, 23, 0));
        obs3.setWeather("阴");
        obs3.setSeeing(2);
        obs3.setMoonPhase(MoonPhase.FIRST_QUARTER);

        bodyStar = new CelestialBody();
        bodyStar.setBodyId(10);
        bodyStar.setName("测试恒星");
        bodyStar.setType(BodyType.STAR);

        bodyPlanet = new CelestialBody();
        bodyPlanet.setBodyId(20);
        bodyPlanet.setName("测试行星");
        bodyPlanet.setType(BodyType.PLANET);
    }

    // ST-001
    @Test
    void totalObservations_returnsCorrectCount() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        assertEquals(3, statsService.totalObservations(1));
    }

    // ST-002
    @Test
    void distinctBodiesObserved_countsDistinctBodyIds() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        assertEquals(2, statsService.distinctBodiesObserved(1));
    }

    // ST-003
    @Test
    void totalEquipmentUsed_countsDistinctEquipment() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2));
        when(obsDao.getLinkedEquipmentIds(1)).thenReturn(Arrays.asList(1, 2));
        when(obsDao.getLinkedEquipmentIds(2)).thenReturn(Arrays.asList(1, 3));
        assertEquals(3, statsService.totalEquipmentUsed(1));
    }

    // ST-004
    @Test
    void countByYear_groupsByYear() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        Map<Integer, Long> result = statsService.countByYear(1);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(2024));
        assertEquals(1L, result.get(2025));
    }

    // ST-005
    @Test
    void countByMonth_groupsByMonth() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        Map<YearMonth, Long> result = statsService.countByMonth(1);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(YearMonth.of(2024, 3)));
        assertEquals(1L, result.get(YearMonth.of(2024, 6)));
        assertEquals(1L, result.get(YearMonth.of(2025, 1)));
    }

    // ST-006
    @Test
    void countByBodyType_groupsByType() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        when(bodyDao.findById(10)).thenReturn(bodyStar);
        when(bodyDao.findById(20)).thenReturn(bodyPlanet);
        Map<BodyType, Long> result = statsService.countByBodyType(1);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(BodyType.STAR));
        assertEquals(1L, result.get(BodyType.PLANET));
    }

    // ST-007
    @Test
    void countBySeeing_groupsBySeeing() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        Map<Integer, Long> result = statsService.countBySeeing(1);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(4));
        assertEquals(1L, result.get(3));
        assertEquals(1L, result.get(2));
    }

    // ST-008
    @Test
    void countByWeather_groupsByWeather() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        Map<String, Long> result = statsService.countByWeather(1);
        assertEquals(2, result.size());
        assertEquals(2L, result.get("晴"));
        assertEquals(1L, result.get("阴"));
    }

    // ST-009
    @Test
    void countByMoonPhase_groupsByMoonPhase() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        Map<String, Long> result = statsService.countByMoonPhase(1);
        assertEquals(3, result.size());
        assertEquals(1L, result.get("满月"));
        assertEquals(1L, result.get("新月"));
        assertEquals(1L, result.get("上弦月"));
    }

    // ST-010
    @Test
    void dailyCounts_groupsByDate() {
        when(obsDao.findAllByUserId(1)).thenReturn(Arrays.asList(obs1, obs2, obs3));
        Map<LocalDate, Long> result = statsService.dailyCounts(1);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(LocalDate.of(2024, 3, 15)));
        assertEquals(1L, result.get(LocalDate.of(2024, 6, 20)));
        assertEquals(1L, result.get(LocalDate.of(2025, 1, 10)));
    }
}
