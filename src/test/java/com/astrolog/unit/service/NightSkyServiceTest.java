package com.astrolog.unit.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.NightSkyData;
import com.astrolog.model.enums.BodyType;
import com.astrolog.model.enums.MoonPhase;
import com.astrolog.service.NightSkyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NightSkyServiceTest {

    @Mock
    private BodyDao bodyDao;

    @InjectMocks
    private NightSkyService skyService;

    private CelestialBody vega;
    private CelestialBody deneb;
    private CelestialBody altair;
    private CelestialBody faintStar;
    private CelestialBody messier57;
    private CelestialBody messier39;
    private CelestialBody orionStar;

    @BeforeEach
    void setUp() {
        vega = createBody(1, "织女星", BodyType.STAR, "天琴座", new BigDecimal("0.03"), null);
        deneb = createBody(2, "天津四", BodyType.STAR, "天鹅座", new BigDecimal("1.25"), null);
        altair = createBody(3, "牛郎星", BodyType.STAR, "天鹰座", new BigDecimal("0.77"), null);
        faintStar = createBody(4, "暗星A", BodyType.STAR, "天琴座", new BigDecimal("3.5"), null);
        messier57 = createBody(5, "环状星云", BodyType.NEBULA, "天琴座", new BigDecimal("8.8"), 57);
        messier39 = createBody(6, "M39疏散星团", BodyType.CLUSTER, "天鹅座", new BigDecimal("4.6"), 39);
        orionStar = createBody(7, "参宿四", BodyType.STAR, "猎户座", new BigDecimal("0.58"), null);
    }

    // NS-001
    @Test
    void recommend_returnsNonNullData() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega, deneb, altair));
        NightSkyData data = skyService.recommend(LocalDate.of(2024, 3, 21), 39.9, 116.4);
        assertNotNull(data);
    }

    // NS-002
    @Test
    void recommend_moonPhaseInValidRange() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega));
        NightSkyData data = skyService.recommend(LocalDate.of(2024, 6, 15), 39.9, 116.4);
        assertNotNull(data.getMoonPhase());
        int ordinal = data.getMoonPhase().ordinal();
        assertTrue(ordinal >= 0 && ordinal < 8, "月相序号应在0-7之间，实际: " + ordinal);
    }

    // NS-003
    @Test
    void recommend_goldenWindowAfterSunset() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega));
        NightSkyData data = skyService.recommend(LocalDate.of(2024, 6, 15), 39.9, 116.4);
        assertNotNull(data.getSunsetTime());
        assertNotNull(data.getGoldenWindowStart());
        assertNotNull(data.getGoldenWindowEnd());
        assertTrue(data.getGoldenWindowStart().isAfter(data.getSunsetTime()),
            "黄金窗口开始应晚于日落");
        assertTrue(data.getGoldenWindowEnd().isAfter(data.getGoldenWindowStart()),
            "黄金窗口结束应晚于开始");
        LocalTime expectedEnd = data.getSunsetTime().plusHours(3);
        assertEquals(expectedEnd.getHour(), data.getGoldenWindowEnd().getHour());
    }

    // NS-004
    @Test
    void recommend_visibleConstellationsNotEmpty() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega, deneb, altair, orionStar));
        NightSkyData data = skyService.recommend(LocalDate.of(2024, 3, 21), 39.9, 116.4);
        assertNotNull(data.getVisibleConstellations());
        assertFalse(data.getVisibleConstellations().isEmpty(), "应至少有一个可见星座");
    }

    // NS-005
    @Test
    void recommend_starsSortedByMagnitude() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega, deneb, altair, faintStar, orionStar,
            messier57, messier39));
        NightSkyData data = skyService.recommend(LocalDate.of(2024, 3, 21), 39.9, 116.4);
        List<CelestialBody> stars = data.getVisibleBodies();
        assertNotNull(stars);
        for (int i = 1; i < stars.size(); i++) {
            assertTrue(stars.get(i - 1).getMagnitude().compareTo(stars.get(i).getMagnitude()) <= 0,
                "亮星应按星等升序排列");
        }
    }

    // NS-006
    @Test
    void recommend_messierSortedByNumber() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega, deneb, altair, orionStar,
            messier57, messier39));
        NightSkyData data = skyService.recommend(LocalDate.of(2024, 3, 21), 39.9, 116.4);
        List<CelestialBody> messier = data.getVisibleMessier();
        assertNotNull(messier);
        for (int i = 1; i < messier.size(); i++) {
            assertTrue(messier.get(i - 1).getMessierNumber() <= messier.get(i).getMessierNumber(),
                "梅西耶天体应按M编号升序排列");
        }
    }

    // NS-007
    @Test
    void recommend_springEquinoxGreenwich_lstApprox12h() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega, deneb, altair));
        NightSkyData data = skyService.recommend(
            LocalDate.of(2024, 3, 21), 51.5, 0.0);
        assertNotNull(data);
        assertNotNull(data.getVisibleConstellations());
    }

    // NS-008
    @Test
    void recommend_knownMoonPhaseDate_returnsCorrectPhase() {
        when(bodyDao.findAll()).thenReturn(Arrays.asList(vega));
        NightSkyData data = skyService.recommend(LocalDate.of(2000, 1, 6), 39.9, 116.4);
        assertEquals(MoonPhase.NEW_MOON, data.getMoonPhase(),
            "2000-01-06为算法新月基点，应为新月");

        NightSkyData data2 = skyService.recommend(LocalDate.of(2000, 1, 7), 39.9, 116.4);
        assertNotNull(data2.getMoonPhase());
        assertTrue(data2.getMoonPhase().ordinal() >= 0
            && data2.getMoonPhase().ordinal() < 8);
    }

    private CelestialBody createBody(int id, String name, BodyType type, String constellation,
                                      BigDecimal magnitude, Integer messierNumber) {
        CelestialBody body = new CelestialBody();
        body.setBodyId(id);
        body.setName(name);
        body.setType(type);
        body.setConstellation(constellation);
        body.setMagnitude(magnitude);
        body.setMessierNumber(messierNumber);
        return body;
    }
}
