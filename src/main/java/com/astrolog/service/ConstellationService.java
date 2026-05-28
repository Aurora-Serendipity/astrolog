package com.astrolog.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.ConstellationInfo;
import com.astrolog.util.JsonDataLoader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConstellationService {

    private final List<ConstellationInfo> constellations;

    public ConstellationService() {
        this.constellations = JsonDataLoader.loadList(
            "data/constellations.json", ConstellationInfo[].class);
    }

    ConstellationService(List<ConstellationInfo> testData) {
        this.constellations = testData;
    }

    public List<ConstellationInfo> getAll() {
        return Collections.unmodifiableList(constellations);
    }

    public ConstellationInfo getByName(String name) {
        return constellations.stream()
            .filter(c -> c.getName().equals(name))
            .findFirst().orElse(null);
    }

    public List<ConstellationInfo> filterBySeason(String season) {
        return constellations.stream()
            .filter(c -> season == null || season.isEmpty()
                || c.getSeason().equals(season))
            .collect(Collectors.toList());
    }

    public List<ConstellationInfo> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) return constellations;
        return constellations.stream()
            .filter(c -> c.getName().contains(keyword)
                || c.getAbbreviation().toLowerCase().contains(
                    keyword.toLowerCase())
                || c.getBrightestStar().contains(keyword))
            .collect(Collectors.toList());
    }

    public List<CelestialBody> getAllBodies() {
        BodyDao bodyDao = new BodyDao();
        return bodyDao.findAll();
    }
}
