package com.astrolog.model.enums;

public enum EquipStatus {
    ACTIVE("在用"),
    MAINTENANCE("维修"),
    RETIRED("退役");

    private final String displayName;

    EquipStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EquipStatus fromString(String value) {
        if (value == null) return null;
        for (EquipStatus status : values()) {
            if (status.displayName.equals(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown EquipStatus: " + value);
    }
}
