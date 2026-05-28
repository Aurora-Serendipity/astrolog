package com.astrolog.service;

import com.astrolog.dao.SiteDao;
import com.astrolog.model.ObservationSite;

import java.util.List;

public class SiteService {

    private final SiteDao siteDao;

    public SiteService() {
        this.siteDao = new SiteDao();
    }

    SiteService(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    public ServiceResult addSite(ObservationSite site) {
        if (site.getName() == null || site.getName().trim().isEmpty()) {
            return ServiceResult.fail("地点名称不能为空");
        }
        if (site.getLatitude() != null) {
            double lat = site.getLatitude().doubleValue();
            if (lat < -90 || lat > 90) {
                return ServiceResult.fail("纬度必须在 -90 到 90 之间");
            }
        }
        if (site.getLongitude() != null) {
            double lon = site.getLongitude().doubleValue();
            if (lon < -180 || lon > 180) {
                return ServiceResult.fail("经度必须在 -180 到 180 之间");
            }
        }
        if (site.getBortleScale() < 1 || site.getBortleScale() > 9) {
            return ServiceResult.fail("波特尔暗空等级必须在 1-9 之间");
        }

        int siteId = siteDao.insert(site);
        return siteId > 0
            ? ServiceResult.success("地点已添加") : ServiceResult.fail("添加失败");
    }

    public ServiceResult updateSite(ObservationSite site) {
        if (site.getName() == null || site.getName().trim().isEmpty()) {
            return ServiceResult.fail("地点名称不能为空");
        }
        if (site.getLatitude() != null) {
            double lat = site.getLatitude().doubleValue();
            if (lat < -90 || lat > 90) {
                return ServiceResult.fail("纬度必须在 -90 到 90 之间");
            }
        }
        if (site.getLongitude() != null) {
            double lon = site.getLongitude().doubleValue();
            if (lon < -180 || lon > 180) {
                return ServiceResult.fail("经度必须在 -180 到 180 之间");
            }
        }
        if (site.getBortleScale() < 1 || site.getBortleScale() > 9) {
            return ServiceResult.fail("波特尔暗空等级必须在 1-9 之间");
        }

        boolean updated = siteDao.update(site);
        return updated
            ? ServiceResult.success("地点已更新") : ServiceResult.fail("更新失败");
    }

    public ServiceResult deleteSite(int siteId) {
        boolean deleted = siteDao.delete(siteId);
        return deleted
            ? ServiceResult.success("地点已删除") : ServiceResult.fail("删除失败");
    }

    public ObservationSite getSite(int siteId) {
        return siteDao.findById(siteId);
    }

    public List<ObservationSite> listByUser(int userId) {
        return siteDao.findAllByUserId(userId);
    }
}
