package com.astrolog.model;

public class ObsEquipment {
    private int obsId;
    private int equipId;

    public ObsEquipment() {}

    public ObsEquipment(int obsId, int equipId) {
        this.obsId = obsId;
        this.equipId = equipId;
    }

    public int getObsId() { return obsId; }
    public void setObsId(int obsId) { this.obsId = obsId; }

    public int getEquipId() { return equipId; }
    public void setEquipId(int equipId) { this.equipId = equipId; }

    @Override
    public String toString() {
        return "ObsEquipment{obsId=" + obsId + ", equipId=" + equipId + "}";
    }
}
