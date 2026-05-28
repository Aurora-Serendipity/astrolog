package com.astrolog.model;

import com.astrolog.model.enums.BodyType;
import java.math.BigDecimal;

public class CelestialBody {
    private int bodyId;
    private String name;
    private BodyType type;
    private String constellation;
    private int raH;
    private int raM;
    private int decDeg;
    private int decMin;
    private BigDecimal magnitude;
    private BigDecimal distanceLy;
    private Integer messierNumber;
    private Integer ngcNumber;
    private String bestSeason;
    private String description;
    private String imagePath;

    public CelestialBody() {}

    public CelestialBody(int bodyId, String name, BodyType type, String constellation,
                         int raH, int raM, int decDeg, int decMin, BigDecimal magnitude,
                         BigDecimal distanceLy, Integer messierNumber, Integer ngcNumber,
                         String bestSeason, String description, String imagePath) {
        this.bodyId = bodyId;
        this.name = name;
        this.type = type;
        this.constellation = constellation;
        this.raH = raH;
        this.raM = raM;
        this.decDeg = decDeg;
        this.decMin = decMin;
        this.magnitude = magnitude;
        this.distanceLy = distanceLy;
        this.messierNumber = messierNumber;
        this.ngcNumber = ngcNumber;
        this.bestSeason = bestSeason;
        this.description = description;
        this.imagePath = imagePath;
    }

    public int getBodyId() { return bodyId; }
    public void setBodyId(int bodyId) { this.bodyId = bodyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BodyType getType() { return type; }
    public void setType(BodyType type) { this.type = type; }

    public String getConstellation() { return constellation; }
    public void setConstellation(String constellation) { this.constellation = constellation; }

    public int getRaH() { return raH; }
    public void setRaH(int raH) { this.raH = raH; }

    public int getRaM() { return raM; }
    public void setRaM(int raM) { this.raM = raM; }

    public int getDecDeg() { return decDeg; }
    public void setDecDeg(int decDeg) { this.decDeg = decDeg; }

    public int getDecMin() { return decMin; }
    public void setDecMin(int decMin) { this.decMin = decMin; }

    public BigDecimal getMagnitude() { return magnitude; }
    public void setMagnitude(BigDecimal magnitude) { this.magnitude = magnitude; }

    public BigDecimal getDistanceLy() { return distanceLy; }
    public void setDistanceLy(BigDecimal distanceLy) { this.distanceLy = distanceLy; }

    public Integer getMessierNumber() { return messierNumber; }
    public void setMessierNumber(Integer messierNumber) { this.messierNumber = messierNumber; }

    public Integer getNgcNumber() { return ngcNumber; }
    public void setNgcNumber(Integer ngcNumber) { this.ngcNumber = ngcNumber; }

    public String getBestSeason() { return bestSeason; }
    public void setBestSeason(String bestSeason) { this.bestSeason = bestSeason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return "CelestialBody{bodyId=" + bodyId + ", name='" + name + "', type=" + type + "}";
    }
}
