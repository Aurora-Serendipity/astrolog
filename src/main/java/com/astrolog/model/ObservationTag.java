package com.astrolog.model;

public class ObservationTag {
    private int tagId;
    private String name;
    private String color;

    public ObservationTag() {}

    public ObservationTag(int tagId, String name, String color) {
        this.tagId = tagId;
        this.name = name;
        this.color = color;
    }

    public int getTagId() { return tagId; }
    public void setTagId(int tagId) { this.tagId = tagId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() {
        return "ObservationTag{tagId=" + tagId + ", name='" + name + "'}";
    }
}
