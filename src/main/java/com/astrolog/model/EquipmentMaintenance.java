package com.astrolog.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EquipmentMaintenance {
    private int maintId;
    private int equipId;
    private LocalDate maintDate;
    private String description;
    private BigDecimal cost;
    private LocalDate nextMaintDate;

    public EquipmentMaintenance() {}

    public EquipmentMaintenance(int maintId, int equipId, LocalDate maintDate,
                                String description, BigDecimal cost,
                                LocalDate nextMaintDate) {
        this.maintId = maintId;
        this.equipId = equipId;
        this.maintDate = maintDate;
        this.description = description;
        this.cost = cost;
        this.nextMaintDate = nextMaintDate;
    }

    public int getMaintId() { return maintId; }
    public void setMaintId(int maintId) { this.maintId = maintId; }

    public int getEquipId() { return equipId; }
    public void setEquipId(int equipId) { this.equipId = equipId; }

    public LocalDate getMaintDate() { return maintDate; }
    public void setMaintDate(LocalDate maintDate) { this.maintDate = maintDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public LocalDate getNextMaintDate() { return nextMaintDate; }
    public void setNextMaintDate(LocalDate nextMaintDate) { this.nextMaintDate = nextMaintDate; }

    @Override
    public String toString() {
        return "EquipmentMaintenance{maintId=" + maintId + ", equipId=" + equipId + "}";
    }
}
