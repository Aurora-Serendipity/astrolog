package com.astrolog.service;

import com.astrolog.dao.EquipDao;
import com.astrolog.dao.MaintDao;
import com.astrolog.model.Equipment;
import com.astrolog.model.EquipmentMaintenance;

import java.util.List;
import java.util.Map;

public class EquipService {

    private final EquipDao equipDao;
    private final MaintDao maintDao;

    public EquipService() {
        this.equipDao = new EquipDao();
        this.maintDao = new MaintDao();
    }

    EquipService(EquipDao equipDao, MaintDao maintDao) {
        this.equipDao = equipDao;
        this.maintDao = maintDao;
    }

    // ==================== 器材 CRUD ====================

    public ServiceResult addEquipment(Equipment equip) {
        if (equip.getName() == null || equip.getName().trim().isEmpty()) {
            return ServiceResult.fail("器材名称不能为空");
        }
        if (equip.getType() == null) {
            return ServiceResult.fail("器材类型不能为空");
        }
        int id = equipDao.insert(equip);
        if (id > 0) {
            return ServiceResult.success("器材已添加");
        }
        return ServiceResult.fail("添加失败");
    }

    public ServiceResult updateEquipment(Equipment equip) {
        if (equip.getName() == null || equip.getName().trim().isEmpty()) {
            return ServiceResult.fail("器材名称不能为空");
        }
        if (equip.getType() == null) {
            return ServiceResult.fail("器材类型不能为空");
        }
        if (equipDao.update(equip)) {
            return ServiceResult.success("器材信息已更新");
        }
        return ServiceResult.fail("更新失败");
    }

    public ServiceResult deleteEquipment(int equipId) {
        if (equipDao.delete(equipId)) {
            return ServiceResult.success("器材已删除");
        }
        return ServiceResult.fail("删除失败");
    }

    // ==================== 查询 ====================

    public Equipment getEquipment(int equipId) {
        return equipDao.findById(equipId);
    }

    public List<Equipment> listByUser(int userId) {
        return equipDao.findAllByUserId(userId);
    }

    public List<Equipment> listByUsage(int userId) {
        return equipDao.findAllSortedByUsage(userId);
    }

    public List<Equipment> searchByName(int userId, String keyword) {
        return equipDao.searchByName(userId, keyword);
    }

    public int getUsageCount(int equipId) {
        return equipDao.getUsageCount(equipId);
    }

    // ==================== 维护日志管理 ====================

    public ServiceResult addMaintenance(EquipmentMaintenance m) {
        if (m.getEquipId() <= 0) {
            return ServiceResult.fail("器材 ID 无效");
        }
        if (m.getMaintDate() == null) {
            return ServiceResult.fail("维护日期不能为空");
        }
        int id = maintDao.insert(m);
        if (id > 0) {
            return ServiceResult.success("维护记录已添加");
        }
        return ServiceResult.fail("添加失败");
    }

    public ServiceResult updateMaintenance(EquipmentMaintenance m) {
        if (maintDao.update(m)) {
            return ServiceResult.success("维护记录已更新");
        }
        return ServiceResult.fail("更新失败");
    }

    public ServiceResult deleteMaintenance(int maintId) {
        if (maintDao.delete(maintId)) {
            return ServiceResult.success("维护记录已删除");
        }
        return ServiceResult.fail("删除失败");
    }

    public List<EquipmentMaintenance> getMaintenanceHistory(int equipId) {
        return maintDao.findByEquipId(equipId);
    }

    public List<EquipmentMaintenance> getUpcomingMaintenance(int userId) {
        return maintDao.findUpcoming(userId);
    }

    public Map<Integer, Integer> batchGetUsageCounts(List<Integer> equipIds) {
        return equipDao.batchGetUsageCounts(equipIds);
    }
}
