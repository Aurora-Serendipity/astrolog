package com.astrolog.model.enums;

public enum BodyType {
    STAR("恒星"),
    PLANET("行星"),
    NEBULA("星云"),
    CLUSTER("星团"),
    GALAXY("星系");

    private final String displayName;

    BodyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BodyType fromString(String value) {
        if (value == null) return null;
        for (BodyType type : values()) {
            if (type.displayName.equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown BodyType: " + value);
    }
}
