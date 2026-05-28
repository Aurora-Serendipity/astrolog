package com.astrolog.util;

import com.astrolog.model.enums.MoonPhase;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DateUtil {
    private DateUtil() {}

    public static MoonPhase calculateMoonPhase(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        double c, e, jd;
        int b;
        if (month < 3) { year--; month += 12; }
        month++;
        c = 365.25 * year;
        e = 30.6 * month;
        jd = c + e + day - 694039.09;
        jd /= 29.5305882;
        b = (int) jd;
        jd -= b;
        b = (int) Math.round(jd * 8);
        if (b >= 8) b = 0;

        MoonPhase[] phases = MoonPhase.values();
        return phases[b];
    }

    public static boolean isGoldenWindow(LocalDateTime time, double lat, double lon) {
        int hour = time.getHour();
        return hour >= 19 && hour <= 23;
    }

    public static String formatLat(double lat) {
        char dir = lat >= 0 ? 'N' : 'S';
        double abs = Math.abs(lat);
        int deg = (int) abs;
        int min = (int) Math.round((abs - deg) * 60);
        return deg + "°" + min + "'" + dir;
    }

    public static String formatLon(double lon) {
        char dir = lon >= 0 ? 'E' : 'W';
        double abs = Math.abs(lon);
        int deg = (int) abs;
        int min = (int) Math.round((abs - deg) * 60);
        return deg + "°" + min + "'" + dir;
    }

    public static String formatRADec(int raH, int raM, int decDeg, int decMin) {
        String decSign = decDeg >= 0 ? "+" : "";
        return "RA " + raH + "h" + raM + "m, Dec " + decSign + decDeg + "°" + Math.abs(decMin) + "'";
    }
}
