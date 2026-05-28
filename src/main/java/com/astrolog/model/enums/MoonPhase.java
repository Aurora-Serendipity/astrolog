package com.astrolog.model.enums;

public enum MoonPhase {
    NEW_MOON("新月"),
    WAXING_CRESCENT("蛾眉月"),
    FIRST_QUARTER("上弦月"),
    WAXING_GIBBOUS("盈凸月"),
    FULL_MOON("满月"),
    WANING_GIBBOUS("亏凸月"),
    LAST_QUARTER("下弦月"),
    WANING_CRESCENT("残月");

    private final String displayName;

    MoonPhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MoonPhase fromString(String value) {
        if (value == null) return null;
        for (MoonPhase phase : values()) {
            if (phase.displayName.equals(value) || phase.name().equalsIgnoreCase(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown MoonPhase: " + value);
    }
}
