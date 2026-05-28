package com.astrolog.model.enums;

public enum UserRole {
    OBSERVER("observer"),
    ADMIN("admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String s) {
        if (s == null) return OBSERVER;
        for (UserRole r : values()) {
            if (r.name().equalsIgnoreCase(s) || r.displayName.equals(s)) {
                return r;
            }
        }
        return OBSERVER;
    }
}
