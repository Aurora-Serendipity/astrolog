package com.astrolog.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.FavoriteDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.enums.BodyType;

import java.math.BigDecimal;
import java.util.List;

public class BodyService {

    private final BodyDao bodyDao;
    private final FavoriteDao favoriteDao;

    public BodyService() {
        this.bodyDao = new BodyDao();
        this.favoriteDao = new FavoriteDao();
    }

    BodyService(BodyDao bodyDao, FavoriteDao favoriteDao) {
        this.bodyDao = bodyDao;
        this.favoriteDao = favoriteDao;
    }

    // ==================== CRUD ====================

    public ServiceResult addBody(CelestialBody body, int operatorUserId) {
        if (body.getName() == null || body.getName().trim().isEmpty()) {
            return ServiceResult.fail("星体名称不能为空");
        }
        if (body.getType() == null) {
            return ServiceResult.fail("星体类型不能为空");
        }
        if (body.getRaH() < 0 || body.getRaH() > 23
                || body.getRaM() < 0 || body.getRaM() > 59) {
            return ServiceResult.fail("赤经格式无效（0-23h, 0-59m）");
        }
        if (body.getDecDeg() < -90 || body.getDecDeg() > 90
                || body.getDecMin() < 0 || body.getDecMin() > 59) {
            return ServiceResult.fail("赤纬格式无效（-90~90°, 0-59'）");
        }
        int id = bodyDao.insert(body);
        if (id > 0) {
            return ServiceResult.success("星体已添加（ID: " + id + "）");
        }
        return ServiceResult.fail("添加失败，请重试");
    }

    public ServiceResult updateBody(CelestialBody body, int operatorUserId) {
        if (body.getBodyId() <= 0) {
            return ServiceResult.fail("星体ID无效");
        }
        if (body.getName() == null || body.getName().trim().isEmpty()) {
            return ServiceResult.fail("星体名称不能为空");
        }
        if (body.getType() == null) {
            return ServiceResult.fail("星体类型不能为空");
        }
        if (body.getRaH() < 0 || body.getRaH() > 23
                || body.getRaM() < 0 || body.getRaM() > 59) {
            return ServiceResult.fail("赤经格式无效（0-23h, 0-59m）");
        }
        if (body.getDecDeg() < -90 || body.getDecDeg() > 90
                || body.getDecMin() < 0 || body.getDecMin() > 59) {
            return ServiceResult.fail("赤纬格式无效（-90~90°, 0-59'）");
        }
        if (bodyDao.update(body)) {
            return ServiceResult.success("星体信息已更新");
        }
        return ServiceResult.fail("更新失败");
    }

    public ServiceResult deleteBody(int bodyId, int operatorUserId) {
        if (bodyDao.delete(bodyId)) {
            return ServiceResult.success("星体已删除");
        }
        return ServiceResult.fail("删除失败");
    }

    // ==================== 查询 ====================

    public CelestialBody getBody(int bodyId) {
        return bodyDao.findById(bodyId);
    }

    public List<CelestialBody> listAll() {
        return bodyDao.findAll();
    }

    public List<CelestialBody> search(String constellation, String type,
                                       BigDecimal minMag, BigDecimal maxMag,
                                       String season, String keyword) {
        return bodyDao.search(constellation, type, minMag, maxMag, season, keyword);
    }

    public List<CelestialBody> listByPopularity() {
        return bodyDao.findByPopularity();
    }

    // ==================== 收藏管理 ====================

    public ServiceResult toggleFavorite(int userId, int bodyId) {
        if (favoriteDao.exists(userId, bodyId)) {
            favoriteDao.remove(userId, bodyId);
            return ServiceResult.success("已取消收藏");
        } else {
            favoriteDao.add(userId, bodyId);
            return ServiceResult.success("已添加收藏");
        }
    }

    public boolean isFavorited(int userId, int bodyId) {
        return favoriteDao.exists(userId, bodyId);
    }

    public List<CelestialBody> getFavorites(int userId) {
        return favoriteDao.findBodiesByUser(userId);
    }

    // ==================== CSV 导入 ====================

    public ImportResult importCsv(String csvContent, int operatorUserId) {
        ImportResult result = new ImportResult();
        String[] lines = csvContent.split("\\R");
        if (lines.length < 2) {
            result.addError(0, "CSV 至少需要标题行和一行数据");
            return result;
        }

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            try {
                String[] cols = line.split(",", -1);
                if (cols.length < 5) {
                    result.addError(i, "列数不足（至少需要 name,type,constellation,ra,mag）");
                    continue;
                }

                CelestialBody body = new CelestialBody();
                body.setName(cols[0].trim());
                body.setType(BodyType.fromString(cols[1].trim()));
                body.setConstellation(cols[2].trim());

                parseRA(cols[3].trim(), body);
                parseDec(cols[4].trim(), body);

                if (cols.length > 5 && !cols[5].isEmpty()) {
                    body.setMagnitude(new BigDecimal(cols[5].trim()));
                }
                if (cols.length > 6 && !cols[6].isEmpty()) {
                    body.setBestSeason(cols[6].trim());
                }
                if (cols.length > 7 && !cols[7].isEmpty()) {
                    body.setDescription(cols[7].trim());
                }
                if (cols.length > 8 && !cols[8].isEmpty()) {
                    body.setMessierNumber(Integer.parseInt(cols[8].trim()));
                }
                if (cols.length > 9 && !cols[9].isEmpty()) {
                    body.setNgcNumber(Integer.parseInt(cols[9].trim()));
                }

                ServiceResult sr = addBody(body, operatorUserId);
                if (sr.isSuccess()) {
                    result.addSuccess(i);
                } else {
                    result.addError(i, sr.getMessage());
                }
            } catch (Exception ex) {
                result.addError(i, "解析失败: " + ex.getMessage());
            }
        }
        return result;
    }

    private void parseRA(String ra, CelestialBody body) {
        if (ra.contains("h")) {
            String[] parts = ra.split("h");
            body.setRaH(Integer.parseInt(parts[0].trim()));
            body.setRaM(Integer.parseInt(parts[1].replace("m", "").trim()));
        } else {
            double decimal = Double.parseDouble(ra);
            body.setRaH((int) decimal);
            body.setRaM((int) ((decimal - body.getRaH()) * 60));
        }
    }

    private void parseDec(String dec, CelestialBody body) {
        if (dec.contains("°")) {
            String[] parts = dec.split("°");
            body.setDecDeg(Integer.parseInt(parts[0].trim()));
            String minPart = parts[1].replace("'", "").trim();
            body.setDecMin(Integer.parseInt(minPart));
        } else {
            double decimal = Double.parseDouble(dec);
            body.setDecDeg((int) decimal);
            double absMin = Math.abs(decimal - body.getDecDeg()) * 60;
            body.setDecMin((int) absMin);
        }
    }
}
