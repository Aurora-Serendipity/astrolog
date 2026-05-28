package com.astrolog.service;

import com.astrolog.dao.ObsDao;
import com.astrolog.dao.TagDao;
import com.astrolog.model.Observation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ObsService {

    private final ObsDao obsDao;
    private final TagDao tagDao;

    public ObsService() {
        this.obsDao = new ObsDao();
        this.tagDao = new TagDao();
    }

    ObsService(ObsDao obsDao, TagDao tagDao) {
        this.obsDao = obsDao;
        this.tagDao = tagDao;
    }

    // ==================== 观测 CRUD ====================

    public ServiceResult addObservation(Observation obs,
                                         List<Integer> equipIds,
                                         List<String> tagNames) {
        if (obs.getBodyId() <= 0) {
            return ServiceResult.fail("请选择观测星体");
        }
        if (obs.getObsTime() == null) {
            return ServiceResult.fail("观测时间不能为空");
        }
        if (obs.getObsTime().isAfter(LocalDateTime.now())) {
            return ServiceResult.fail("观测时间不能在未来");
        }
        if (obs.getSeeing() < 1 || obs.getSeeing() > 5) {
            return ServiceResult.fail("视宁度必须在 1-5 之间");
        }

        int obsId = obsDao.insert(obs);
        if (obsId <= 0) {
            return ServiceResult.fail("添加失败");
        }

        if (equipIds != null) {
            for (int equipId : equipIds) {
                obsDao.linkEquipment(obsId, equipId);
            }
        }

        if (tagNames != null) {
            for (String tagName : tagNames) {
                String trimmed = tagName.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                int tagId = tagDao.getOrCreate(trimmed, "#3366CC");
                obsDao.linkTag(obsId, tagId);
            }
        }

        return ServiceResult.success("观测记录已添加");
    }

    public ServiceResult updateObservation(Observation obs,
                                            List<Integer> equipIds,
                                            List<String> tagNames) {
        if (obs.getBodyId() <= 0) {
            return ServiceResult.fail("请选择观测星体");
        }
        if (obs.getObsTime() == null) {
            return ServiceResult.fail("观测时间不能为空");
        }
        if (obs.getObsTime().isAfter(LocalDateTime.now())) {
            return ServiceResult.fail("观测时间不能在未来");
        }
        if (obs.getSeeing() < 1 || obs.getSeeing() > 5) {
            return ServiceResult.fail("视宁度必须在 1-5 之间");
        }

        boolean updated = obsDao.update(obs);
        if (!updated) {
            return ServiceResult.fail("更新失败");
        }

        obsDao.unlinkAllEquipment(obs.getObsId());
        if (equipIds != null) {
            for (int eid : equipIds) {
                obsDao.linkEquipment(obs.getObsId(), eid);
            }
        }

        obsDao.unlinkAllTags(obs.getObsId());
        if (tagNames != null) {
            for (String tn : tagNames) {
                String trimmed = tn.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                int tid = tagDao.getOrCreate(trimmed, "#3366CC");
                obsDao.linkTag(obs.getObsId(), tid);
            }
        }

        return ServiceResult.success("观测记录已更新");
    }

    public ServiceResult deleteObservation(int obsId, int userId) {
        Observation obs = obsDao.findById(obsId);
        if (obs == null) {
            return ServiceResult.fail("记录不存在");
        }
        if (obs.getUserId() != userId) {
            return ServiceResult.fail("无权删除他人记录");
        }
        return obsDao.delete(obsId)
            ? ServiceResult.success("已删除") : ServiceResult.fail("删除失败");
    }

    // ==================== 查询 ====================

    public Observation getObservation(int obsId) {
        return obsDao.findById(obsId);
    }

    public List<Observation> listByUser(int userId) {
        return obsDao.findAllByUserId(userId);
    }

    public List<Observation> search(int userId, LocalDateTime start, LocalDateTime end,
                                     Integer bodyId, Integer siteId, String weather,
                                     Integer seeing, String moonPhase, String keyword) {
        return obsDao.search(userId, start, end, bodyId, siteId, weather, seeing,
            moonPhase, keyword);
    }

    // ==================== 关联查询 ====================

    public List<Integer> getEquipmentIds(int obsId) {
        return obsDao.getLinkedEquipmentIds(obsId);
    }

    public List<String> getTagNames(int obsId) {
        return tagDao.findByObsId(obsId).stream()
            .map(t -> t.getName()).collect(Collectors.toList());
    }
}
