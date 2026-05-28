package com.astrolog.model;

import com.astrolog.model.enums.EquipStatus;
import com.astrolog.model.enums.EquipType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Equipment {
    private int equipId;
    private int userId;
    private String name;
    private EquipType type;
    private BigDecimal aperture;
    private int focalLength;
    private LocalDate purchaseDate;
    private EquipStatus status;
    private String description;

    public Equipment() {}

    public Equipment(int equipId, int userId, String name, EquipType type,
                     BigDecimal aperture, int focalLength, LocalDate purchaseDate,
                     EquipStatus status, String description) {
        this.equipId = equipId;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.aperture = aperture;
        this.focalLength = focalLength;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.description = description;
    }

    public int getEquipId() { return equipId; }
    public void setEquipId(int equipId) { this.equipId = equipId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EquipType getType() { return type; }
    public void setType(EquipType type) { this.type = type; }

    public BigDecimal getAperture() { return aperture; }
    public void setAperture(BigDecimal aperture) { this.aperture = aperture; }

    public int getFocalLength() { return focalLength; }
    public void setFocalLength(int focalLength) { this.focalLength = focalLength; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public EquipStatus getStatus() { return status; }
    public void setStatus(EquipStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Equipment{equipId=" + equipId + ", name='" + name + "', type=" + type + "}";
    }
}
