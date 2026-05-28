package com.astrolog.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.NightSkyData;
import com.astrolog.model.enums.MoonPhase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NightSkyService {

    private final BodyDao bodyDao;

    private static final Map<String, double[]> CONSTELLATION_RA = new LinkedHashMap<>();
    static {
        CONSTELLATION_RA.put("猎户座", new double[]{4.7, 6.4, 0});
        CONSTELLATION_RA.put("大熊座", new double[]{8.1, 14.5, 55});
        CONSTELLATION_RA.put("仙后座", new double[]{0.0, 3.5, 60});
        CONSTELLATION_RA.put("天鹅座", new double[]{19.2, 22.0, 40});
        CONSTELLATION_RA.put("天琴座", new double[]{18.2, 19.2, 36});
        CONSTELLATION_RA.put("天鹰座", new double[]{18.7, 20.6, 3});
        CONSTELLATION_RA.put("天蝎座", new double[]{15.8, 17.9, -30});
        CONSTELLATION_RA.put("金牛座", new double[]{3.4, 6.0, 17});
        CONSTELLATION_RA.put("双子座", new double[]{6.0, 8.1, 22});
        CONSTELLATION_RA.put("狮子座", new double[]{9.3, 11.8, 15});
        CONSTELLATION_RA.put("室女座", new double[]{11.6, 14.4, 0});
        CONSTELLATION_RA.put("牧夫座", new double[]{13.6, 15.8, 30});
    }

    public NightSkyService() {
        this.bodyDao = new BodyDao();
    }

    NightSkyService(BodyDao bodyDao) {
        this.bodyDao = bodyDao;
    }

    public NightSkyData recommend(LocalDate date, double latitude, double longitude) {
        NightSkyData data = new NightSkyData();

        data.setMoonPhase(calculateMoonPhase(date));
        data.setMoonRise(estimateMoonRise(date));
        data.setMoonSet(estimateMoonSet(date));

        LocalTime sunset = estimateSunset(date, latitude, longitude);
        data.setSunsetTime(sunset);
        if (sunset != null) {
            data.setGoldenWindowStart(sunset.plusHours(1));
            data.setGoldenWindowEnd(sunset.plusHours(3));
        }

        double lst = calculateLST(date, longitude);
        double visibleRAMin = (lst - 3 + 24) % 24;
        double visibleRAMax = (lst + 3) % 24;

        List<String> visibleConstellations = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : CONSTELLATION_RA.entrySet()) {
            double[] range = entry.getValue();
            if (isRAInRange(range[0], range[1], visibleRAMin, visibleRAMax)
                    && isDecVisible(range[2], latitude)) {
                visibleConstellations.add(entry.getKey());
            }
        }
        data.setVisibleConstellations(visibleConstellations);

        List<CelestialBody> allBodies = bodyDao.findAll();
        List<CelestialBody> visibleStars = allBodies.stream()
            .filter(b -> b.getConstellation() != null
                && visibleConstellations.contains(b.getConstellation())
                && b.getType().getDisplayName().equals("恒星")
                && b.getMagnitude() != null
                && b.getMagnitude().compareTo(new BigDecimal("3.0")) < 0)
            .sorted(Comparator.comparing(CelestialBody::getMagnitude))
            .collect(Collectors.toList());
        data.setVisibleBodies(visibleStars);

        List<CelestialBody> visibleMessier = allBodies.stream()
            .filter(b -> b.getConstellation() != null
                && visibleConstellations.contains(b.getConstellation())
                && b.getMessierNumber() != null)
            .sorted(Comparator.comparingInt(CelestialBody::getMessierNumber))
            .collect(Collectors.toList());
        data.setVisibleMessier(visibleMessier);

        return data;
    }

    private MoonPhase calculateMoonPhase(LocalDate date) {
        LocalDate newMoonBase = LocalDate.of(2000, 1, 6);
        long days = newMoonBase.until(date).getDays();
        int phaseIndex = (int) (days % 2953 / 100.0 * 8) % 8;
        return MoonPhase.values()[Math.abs(phaseIndex) % 8];
    }

    private LocalTime estimateSunset(LocalDate date, double lat, double lon) {
        int dayOfYear = date.getDayOfYear();
        double offset = -Math.cos(2 * Math.PI * (dayOfYear - 172) / 365) * 75;
        return LocalTime.of(18, 0).plusMinutes((long) offset);
    }

    private LocalTime estimateMoonRise(LocalDate date) {
        MoonPhase phase = calculateMoonPhase(date);
        int phaseOrdinal = phase.ordinal();
        int delayMinutes = (8 - Math.abs(phaseOrdinal - 4)) * 30;
        return LocalTime.of(17, 0).plusMinutes(delayMinutes);
    }

    private LocalTime estimateMoonSet(LocalDate date) {
        return estimateMoonRise(date).plusHours(12);
    }

    private double calculateLST(LocalDate date, double longitude) {
        LocalDate springEquinox = LocalDate.of(date.getYear(), 3, 21);
        long daysSinceEquinox = springEquinox.until(date).getDays();
        double gst = (12.0 + daysSinceEquinox * 0.0657) % 24;
        return (gst + longitude / 15.0 + 24) % 24;
    }

    private boolean isRAInRange(double raMin, double raMax,
                                 double visibleMin, double visibleMax) {
        if (visibleMin <= visibleMax) {
            return !(raMax < visibleMin || raMin > visibleMax);
        } else {
            return !(raMax < visibleMin && raMin > visibleMax);
        }
    }

    private boolean isDecVisible(double dec, double lat) {
        return dec > -(90 - Math.abs(lat)) * 0.5;
    }
}
