package com.astrolog.model;

import com.astrolog.model.enums.MoonPhase;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Observation {
    private int obsId;
    private int userId;
    private int bodyId;
    private Integer siteId;
    private LocalDateTime obsTime;
    private BigDecimal locationLat;
    private BigDecimal locationLon;
    private String weather;
    private int seeing;
    private MoonPhase moonPhase;
    private String note;
    private LocalDateTime createTime;

    public Observation() {}

    public Observation(int obsId, int userId, int bodyId, Integer siteId,
                       LocalDateTime obsTime, BigDecimal locationLat,
                       BigDecimal locationLon, String weather, int seeing,
                       MoonPhase moonPhase, String note, LocalDateTime createTime) {
        this.obsId = obsId;
        this.userId = userId;
        this.bodyId = bodyId;
        this.siteId = siteId;
        this.obsTime = obsTime;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.weather = weather;
        this.seeing = seeing;
        this.moonPhase = moonPhase;
        this.note = note;
        this.createTime = createTime;
    }

    public int getObsId() { return obsId; }
    public void setObsId(int obsId) { this.obsId = obsId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBodyId() { return bodyId; }
    public void setBodyId(int bodyId) { this.bodyId = bodyId; }

    public Integer getSiteId() { return siteId; }
    public void setSiteId(Integer siteId) { this.siteId = siteId; }

    public LocalDateTime getObsTime() { return obsTime; }
    public void setObsTime(LocalDateTime obsTime) { this.obsTime = obsTime; }

    public BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(BigDecimal locationLat) { this.locationLat = locationLat; }

    public BigDecimal getLocationLon() { return locationLon; }
    public void setLocationLon(BigDecimal locationLon) { this.locationLon = locationLon; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public int getSeeing() { return seeing; }
    public void setSeeing(int seeing) { this.seeing = seeing; }

    public MoonPhase getMoonPhase() { return moonPhase; }
    public void setMoonPhase(MoonPhase moonPhase) { this.moonPhase = moonPhase; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "Observation{obsId=" + obsId + ", userId=" + userId + ", bodyId=" + bodyId + "}";
    }
}
