package com.astrolog.model;

import com.astrolog.model.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String password;
    private UserRole role;
    private String avatarPath;
    private String city;
    private BigDecimal defaultLat;
    private BigDecimal defaultLon;
    private int loginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLogin;
    private LocalDateTime createTime;

    public User() {}

    public User(int userId, String username, String password, UserRole role,
                String avatarPath, String city, BigDecimal defaultLat,
                BigDecimal defaultLon, int loginAttempts, LocalDateTime lockedUntil,
                LocalDateTime lastLogin, LocalDateTime createTime) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.avatarPath = avatarPath;
        this.city = city;
        this.defaultLat = defaultLat;
        this.defaultLon = defaultLon;
        this.loginAttempts = loginAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLogin = lastLogin;
        this.createTime = createTime;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BigDecimal getDefaultLat() { return defaultLat; }
    public void setDefaultLat(BigDecimal defaultLat) { this.defaultLat = defaultLat; }

    public BigDecimal getDefaultLon() { return defaultLon; }
    public void setDefaultLon(BigDecimal defaultLon) { this.defaultLon = defaultLon; }

    public int getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(int loginAttempts) { this.loginAttempts = loginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", username='" + username + "', role=" + role + "}";
    }
}
