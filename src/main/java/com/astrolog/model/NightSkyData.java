package com.astrolog.model;

import com.astrolog.model.enums.MoonPhase;
import java.time.LocalTime;
import java.util.List;

public class NightSkyData {
    private MoonPhase moonPhase;
    private LocalTime moonRise;
    private LocalTime moonSet;
    private LocalTime sunsetTime;
    private LocalTime goldenWindowStart;
    private LocalTime goldenWindowEnd;
    private List<String> visibleConstellations;
    private List<CelestialBody> visibleBodies;
    private List<CelestialBody> visibleMessier;

    public NightSkyData() {}

    public MoonPhase getMoonPhase() { return moonPhase; }
    public void setMoonPhase(MoonPhase moonPhase) { this.moonPhase = moonPhase; }

    public LocalTime getMoonRise() { return moonRise; }
    public void setMoonRise(LocalTime moonRise) { this.moonRise = moonRise; }

    public LocalTime getMoonSet() { return moonSet; }
    public void setMoonSet(LocalTime moonSet) { this.moonSet = moonSet; }

    public LocalTime getSunsetTime() { return sunsetTime; }
    public void setSunsetTime(LocalTime sunsetTime) { this.sunsetTime = sunsetTime; }

    public LocalTime getGoldenWindowStart() { return goldenWindowStart; }
    public void setGoldenWindowStart(LocalTime goldenWindowStart) { this.goldenWindowStart = goldenWindowStart; }

    public LocalTime getGoldenWindowEnd() { return goldenWindowEnd; }
    public void setGoldenWindowEnd(LocalTime goldenWindowEnd) { this.goldenWindowEnd = goldenWindowEnd; }

    public List<String> getVisibleConstellations() { return visibleConstellations; }
    public void setVisibleConstellations(List<String> visibleConstellations) { this.visibleConstellations = visibleConstellations; }

    public List<CelestialBody> getVisibleBodies() { return visibleBodies; }
    public void setVisibleBodies(List<CelestialBody> visibleBodies) { this.visibleBodies = visibleBodies; }

    public List<CelestialBody> getVisibleMessier() { return visibleMessier; }
    public void setVisibleMessier(List<CelestialBody> visibleMessier) { this.visibleMessier = visibleMessier; }

    @Override
    public String toString() {
        return "NightSkyData{moonPhase=" + moonPhase
            + ", visibleBodies=" + (visibleBodies != null ? visibleBodies.size() : 0)
            + ", visibleMessier=" + (visibleMessier != null ? visibleMessier.size() : 0)
            + ", constellations=" + (visibleConstellations != null ? visibleConstellations.size() : 0) + "}";
    }
}
