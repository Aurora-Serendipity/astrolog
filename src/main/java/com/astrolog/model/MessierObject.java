package com.astrolog.model;

import java.math.BigDecimal;

public class MessierObject {
    private int messierNumber;
    private String name;
    private String type;
    private String constellation;
    private BigDecimal magnitude;
    private String season;
    private String description;

    public MessierObject() {}

    public MessierObject(int messierNumber, String name, String type, String constellation,
                         BigDecimal magnitude, String season, String description) {
        this.messierNumber = messierNumber;
        this.name = name;
        this.type = type;
        this.constellation = constellation;
        this.magnitude = magnitude;
        this.season = season;
        this.description = description;
    }

    public int getMessierNumber() { return messierNumber; }
    public void setMessierNumber(int messierNumber) { this.messierNumber = messierNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getConstellation() { return constellation; }
    public void setConstellation(String constellation) { this.constellation = constellation; }

    public BigDecimal getMagnitude() { return magnitude; }
    public void setMagnitude(BigDecimal magnitude) { this.magnitude = magnitude; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "MessierObject{messierNumber=" + messierNumber + ", name='" + name + "'}";
    }
}
