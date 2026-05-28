package com.astrolog.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.EquipDao;
import com.astrolog.dao.ObsDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.Equipment;
import com.astrolog.model.Observation;
import com.astrolog.model.enums.BodyType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StatsService {

    private final ObsDao obsDao;
    private final BodyDao bodyDao;
    private final EquipDao equipDao;

    public StatsService() {
        this.obsDao = new ObsDao();
        this.bodyDao = new BodyDao();
        this.equipDao = new EquipDao();
    }

    StatsService(ObsDao obsDao, BodyDao bodyDao, EquipDao equipDao) {
        this.obsDao = obsDao;
        this.bodyDao = bodyDao;
        this.equipDao = equipDao;
    }

    // ==================== 观测频次统计 ====================

    public Map<Integer, Long> countByYear(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getObsTime() != null)
            .collect(Collectors.groupingBy(
                o -> o.getObsTime().getYear(),
                TreeMap::new, Collectors.counting()));
    }

    public Map<YearMonth, Long> countByMonth(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getObsTime() != null)
            .collect(Collectors.groupingBy(
                o -> YearMonth.from(o.getObsTime()),
                TreeMap::new, Collectors.counting()));
    }

    // ==================== 类型分布统计 ====================

    public Map<BodyType, Long> countByBodyType(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .map(o -> bodyDao.findById(o.getBodyId()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                CelestialBody::getType,
                LinkedHashMap::new, Collectors.counting()));
    }

    // ==================== 观测条件分析 ====================

    public Map<Integer, Long> countBySeeing(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getSeeing() > 0)
            .collect(Collectors.groupingBy(
                Observation::getSeeing,
                TreeMap::new, Collectors.counting()));
    }

    public Map<String, Long> countByWeather(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getWeather() != null && !o.getWeather().isEmpty())
            .collect(Collectors.groupingBy(
                Observation::getWeather,
                TreeMap::new, Collectors.counting()));
    }

    public Map<String, Long> countByMoonPhase(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getMoonPhase() != null)
            .collect(Collectors.groupingBy(
                o -> o.getMoonPhase().getDisplayName(),
                TreeMap::new, Collectors.counting()));
    }

    // ==================== 器材使用排行 ====================

    public List<Map.Entry<String, Long>> topEquipment(int userId) {
        List<Equipment> equipList = equipDao.findAllByUserId(userId);
        List<Integer> equipIds = equipList.stream()
            .map(Equipment::getEquipId)
            .collect(Collectors.toList());
        Map<Integer, Integer> usageMap = equipDao.batchGetUsageCounts(equipIds);
        return equipList.stream()
            .map(e -> new AbstractMap.SimpleEntry<>(
                e.getName(),
                (long) usageMap.getOrDefault(e.getEquipId(), 0)))
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    // ==================== 热力图数据 ====================

    public Map<LocalDate, Long> dailyCounts(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getObsTime() != null)
            .collect(Collectors.groupingBy(
                o -> o.getObsTime().toLocalDate(),
                TreeMap::new, Collectors.counting()));
    }

    // ==================== 概览统计 ====================

    public long totalObservations(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.size();
    }

    public long distinctBodiesObserved(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .map(Observation::getBodyId)
            .distinct()
            .count();
    }

    public long totalEquipmentUsed(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .flatMap(o -> obsDao.getLinkedEquipmentIds(o.getObsId()).stream())
            .distinct()
            .count();
    }

    public Set<Integer> observedBodyIds(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .map(Observation::getBodyId)
            .collect(Collectors.toSet());
    }
}
