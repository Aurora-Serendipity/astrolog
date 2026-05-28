package com.astrolog.model;

import java.util.ArrayList;
import java.util.List;

public class ConstellationInfo {
    private String name;
    private String abbreviation;
    private double area;
    private String brightestStar;
    private String season;
    private String mythology;
    private List<ConstellationLine> lines = new ArrayList<>();

    public static class ConstellationLine {
        private double fromRA;
        private double fromDec;
        private double toRA;
        private double toDec;

        public ConstellationLine() {}

        public ConstellationLine(double fromRA, double fromDec, double toRA, double toDec) {
            this.fromRA = fromRA; this.fromDec = fromDec;
            this.toRA = toRA; this.toDec = toDec;
        }

        public double getFromRA() { return fromRA; }
        public void setFromRA(double fromRA) { this.fromRA = fromRA; }
        public double getFromDec() { return fromDec; }
        public void setFromDec(double fromDec) { this.fromDec = fromDec; }
        public double getToRA() { return toRA; }
        public void setToRA(double toRA) { this.toRA = toRA; }
        public double getToDec() { return toDec; }
        public void setToDec(double toDec) { this.toDec = toDec; }
    }

    public ConstellationInfo() {}

    public ConstellationInfo(String name, String abbreviation, double area,
                             String brightestStar, String season, String mythology) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.area = area;
        this.brightestStar = brightestStar;
        this.season = season;
        this.mythology = mythology;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAbbreviation() { return abbreviation; }
    public void setAbbreviation(String abbreviation) { this.abbreviation = abbreviation; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public String getBrightestStar() { return brightestStar; }
    public void setBrightestStar(String brightestStar) { this.brightestStar = brightestStar; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getMythology() { return mythology; }
    public void setMythology(String mythology) { this.mythology = mythology; }

    public List<ConstellationLine> getLines() { return lines; }
    public void setLines(List<ConstellationLine> lines) { this.lines = lines; }

    @Override
    public String toString() {
        return "ConstellationInfo{name='" + name + "', abbreviation='" + abbreviation + "'}";
    }
}
