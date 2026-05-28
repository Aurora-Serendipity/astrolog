package com.astrolog.model;

import java.math.BigDecimal;

public class ObservationSite {
    private int siteId;
    private int userId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int altitude;
    private int bortleScale;
    private String bestTime;

    public ObservationSite() {}

    public ObservationSite(int siteId, int userId, String name, BigDecimal latitude,
                           BigDecimal longitude, int altitude, int bortleScale,
                           String bestTime) {
        this.siteId = siteId;
        this.userId = userId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.bortleScale = bortleScale;
        this.bestTime = bestTime;
    }

    public int getSiteId() { return siteId; }
    public void setSiteId(int siteId) { this.siteId = siteId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public int getAltitude() { return altitude; }
    public void setAltitude(int altitude) { this.altitude = altitude; }

    public int getBortleScale() { return bortleScale; }
    public void setBortleScale(int bortleScale) { this.bortleScale = bortleScale; }

    public String getBestTime() { return bestTime; }
    public void setBestTime(String bestTime) { this.bestTime = bestTime; }

    @Override
    public String toString() {
        return "ObservationSite{siteId=" + siteId + ", name='" + name + "'}";
    }
}
