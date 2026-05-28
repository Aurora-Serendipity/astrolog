package com.astrolog.model.enums;

public enum EquipType {
    TELESCOPE("望远镜"),
    EYEPIECE("目镜"),
    CAMERA("相机"),
    OTHER("其他");

    private final String displayName;

    EquipType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EquipType fromString(String value) {
        if (value == null) return null;
        for (EquipType type : values()) {
            if (type.displayName.equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EquipType: " + value);
    }
}
