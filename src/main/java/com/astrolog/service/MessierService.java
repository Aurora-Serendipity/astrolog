package com.astrolog.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.ObsDao;
import com.astrolog.model.MessierObject;
import com.astrolog.model.Observation;
import com.astrolog.util.JsonDataLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MessierService {

    private final BodyDao bodyDao;
    private final ObsDao obsDao;
    private final List<MessierObject> catalog;

    public MessierService() {
        this.bodyDao = new BodyDao();
        this.obsDao = new ObsDao();
        this.catalog = JsonDataLoader.loadList(
            "data/messier_catalog.json", MessierObject[].class);
    }

    MessierService(BodyDao bodyDao, ObsDao obsDao) {
        this.bodyDao = bodyDao;
        this.obsDao = obsDao;
        this.catalog = new ArrayList<>();
    }

    public List<MessierObject> getFullCatalog() {
        return Collections.unmodifiableList(catalog);
    }

    public List<MessierObject> filterBySeason(String season) {
        return catalog.stream()
            .filter(m -> season == null || season.isEmpty()
                || (m.getSeason() != null && m.getSeason().equals(season)))
            .collect(Collectors.toList());
    }

    public List<MessierObject> filterByType(String type) {
        return catalog.stream()
            .filter(m -> type == null || type.isEmpty()
                || (m.getType() != null && m.getType().equals(type)))
            .collect(Collectors.toList());
    }

    public Set<Integer> getObservedNumbers(int userId) {
        List<Observation> obsList = obsDao.findAllByUserId(userId);
        Set<Integer> observed = new HashSet<>();
        for (Observation obs : obsList) {
            var body = bodyDao.findById(obs.getBodyId());
            if (body != null && body.getMessierNumber() != null) {
                observed.add(body.getMessierNumber());
            }
        }
        return observed;
    }

    public boolean isObserved(int messierNumber, Set<Integer> observed) {
        return observed.contains(messierNumber);
    }

    public double getProgress(Set<Integer> observed) {
        return (double) observed.size() / catalog.size() * 100.0;
    }

    public boolean isCertEligible(Set<Integer> observed) {
        return observed.size() >= catalog.size();
    }

    public Map<String, int[]> getStatsByType(Set<Integer> observed) {
        Map<String, int[]> stats = new LinkedHashMap<>();
        for (MessierObject m : catalog) {
            stats.putIfAbsent(m.getType(), new int[]{0, 0});
            stats.get(m.getType())[0]++;
            if (observed.contains(m.getMessierNumber())) {
                stats.get(m.getType())[1]++;
            }
        }
        return stats;
    }
}
