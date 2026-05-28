package com.astrolog.config;

public final class AppConfig {
    private AppConfig() {}

    public static final String APP_NAME = "AstroLog";
    public static final String APP_VERSION = "1.0-SNAPSHOT";
    public static final double DEFAULT_LAT = 39.9;
    public static final double DEFAULT_LON = 116.4;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCK_DURATION_MINUTES = 30;
}
