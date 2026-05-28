# S5 — 观测记录管理 交接文档

> **子阶段：** S5 观测记录管理
> **所属阶段：** 阶段二 — 功能切片迭代
> **编制日期：** 2026-05-17
> **上级会话状态：** S4 已完成（73/73 测试通过，G1-G4 审核通过），S5 是最大的子阶段

---

## 1. 前置状态速览

### 1.1 已交付资产

| 资产                   | 路径                         | S5 用途                                                                           |
| ---------------------- | ---------------------------- | --------------------------------------------------------------------------------- |
| `observations` 表      | `sql/init.sql`               | 观测记录主表，obs_id/user_id/body_id/site_id/obs_time/...                         |
| `obs_equipment` 表     | `sql/init.sql`               | 观测-器材 M:N 关联，复合主键(obs_id, equip_id)                                    |
| `observation_sites` 表 | `sql/init.sql`               | 观测地点，site_id/user_id/name/latitude/longitude/altitude/bortle_scale/best_time |
| `observation_tags` 表  | `sql/init.sql`               | 标签字典，tag_id/name/color                                                       |
| `obs_tag_relation` 表  | `sql/init.sql`               | 观测-标签 M:N 关联，复合主键(obs_id, tag_id)                                      |
| `Observation` 实体     | `model/Observation.java`     | 13 字段，siteId 为 nullable Integer                                               |
| `ObservationSite` 实体 | `model/ObservationSite.java` | 8 字段                                                                            |
| `ObservationTag` 实体  | `model/ObservationTag.java`  | 3 字段                                                                            |
| `MoonPhase` 枚举       | `model/enums/MoonPhase.java` | 8 种月相 + fromString()                                                           |
| `UserDao`              | `dao/UserDao.java`           | S2，AddObsDialog 获取当前用户默认坐标                                             |
| `BodyDao`              | `dao/BodyDao.java`           | S3，星体选择器数据源；mapRow 为 package-private 可复用                            |
| `EquipDao`             | `dao/EquipDao.java`          | S4，器材多选数据源；mapRow 为 package-private 可复用                              |
| `BaseDao<T>`           | `dao/BaseDao.java`           | executeQuery/executeUpdate/executeInsert                                          |
| `ServiceResult`        | `service/ServiceResult.java` | S2                                                                                |
| `MainFrame`            | `ui/frame/MainFrame.java`    | `getCurrentUserObject()` / `switchTo()`                                           |

### 1.2 MainFrame 当前状态

NAV_LABELS[2] = "我的观测"，当前为占位 JPanel，需替换为 ObservationPanel。

### 1.3 S2-S4 全部偏差经验（S5 必须遵循）

| 经验                                      | S5 应用                                                  |
| ----------------------------------------- | -------------------------------------------------------- |
| COUNT 查询用原生 JDBC 直接取 rs.getInt(1) | TagDao.existsByName                                      |
| executeInsert 仅限有自增主键的表          | obs_equipment/obs_tag_relation 无自增 → 用 executeUpdate |
| UI 下拉中文 ↔ DB 英文转换                 | MoonPhase/天气/季节 筛选前 fromString                    |
| Dialog 构造器末尾必须 setVisible(true)    | AddObsDialog                                             |
| Service 需 package-private 测试构造器     | ObsService(ObsDao, TagDao, SiteDao)                      |
| ServiceResult 消息必须显式展示            | ObservationPanel 状态栏                                  |
| UI 文件禁用通配符 import                  | 所有 S5 UI 文件                                          |
| Unicode 符号用 Font.SANS_SERIF            | 标签色块/状态图标                                        |
| N+1 查询避免                              | 批量查询器材使用计数 → 单次 SQL                          |
| 表单输入包裹数值+日期异常捕获             | AddObsDialog 所有字段                                    |
| 禁止死代码                                | 不过度设计"兼容性检查"类方法                             |

---

## 2. S5 目标与范围

### 2.1 核心目标

实现系统最核心功能：观测记录完整 CRUD，支持关联星体/器材/标签/地点，多条件组合查询。

### 2.2 新增文件清单（12 个）

| #   | 文件                    | 包                       | 行数 |
| --- | ----------------------- | ------------------------ | ---- |
| 1   | `ObsDao.java`           | `com.astrolog.dao`       | ~300 |
| 2   | `TagDao.java`           | `com.astrolog.dao`       | ~90  |
| 3   | `SiteDao.java`          | `com.astrolog.dao`       | ~110 |
| 4   | `ObsService.java`       | `com.astrolog.service`   | ~320 |
| 5   | `SiteService.java`      | `com.astrolog.service`   | ~110 |
| 6   | `ObservationPanel.java` | `com.astrolog.ui.panel`  | ~380 |
| 7   | `AddObsDialog.java`     | `com.astrolog.ui.dialog` | ~280 |
| 8   | `ObsServiceTest.java`   | test `unit/service`      | ~200 |
| 9   | `SiteServiceTest.java`  | test `unit/service`      | ~60  |
| 10  | `ObsDaoTest.java`       | test `integration/dao`   | ~160 |
| 11  | `TagDaoTest.java`       | test `integration/dao`   | ~90  |
| 12  | `SiteDaoTest.java`      | test `integration/dao`   | ~100 |

### 2.3 修改文件（1 个）

| #   | 文件             | 改动                              |
| --- | ---------------- | --------------------------------- |
| 1   | `MainFrame.java` | "我的观测"占位 → ObservationPanel |

### 2.4 范围边界

**不做：** 观测统计图表（S6）、星座分布图（S6）、年度报告导出（S9）、观测日历热力图（S6）

---

## 3. 详细实现规格

### 3.1 ObsDao — 观测记录数据访问（核心）

**文件：** `src/main/java/com/astrolog/dao/ObsDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.Observation;
import com.astrolog.model.enums.MoonPhase;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ObsDao extends BaseDao<Observation> {

    // === 基础 CRUD ===

    public Observation findById(int obsId) {
        String sql = "SELECT obs_id, user_id, body_id, site_id, obs_time, "
                   + "location_lat, location_lon, weather, seeing, moon_phase, "
                   + "note, create_time FROM observations WHERE obs_id = ?";
        List<Observation> results = executeQuery(sql, new Object[]{obsId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Observation> findAllByUserId(int userId) {
        String sql = "SELECT obs_id, user_id, body_id, site_id, obs_time, "
                   + "location_lat, location_lon, weather, seeing, moon_phase, "
                   + "note, create_time FROM observations "
                   + "WHERE user_id = ? ORDER BY obs_time DESC";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public int insert(Observation obs) {
        String sql = "INSERT INTO observations (user_id, body_id, site_id, obs_time, "
                   + "location_lat, location_lon, weather, seeing, moon_phase, note) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            obs.getUserId(), obs.getBodyId(),
            obs.getSiteId(),  // nullable
            Timestamp.valueOf(obs.getObsTime()),
            obs.getLocationLat(), obs.getLocationLon(),
            obs.getWeather(), obs.getSeeing(),
            obs.getMoonPhase() != null
                ? obs.getMoonPhase().name().toLowerCase() : null,
            obs.getNote()});
    }

    public boolean update(Observation obs) {
        String sql = "UPDATE observations SET body_id=?, site_id=?, obs_time=?, "
                   + "location_lat=?, location_lon=?, weather=?, seeing=?, "
                   + "moon_phase=?, note=? WHERE obs_id=?";
        return executeUpdate(sql, new Object[]{
            obs.getBodyId(), obs.getSiteId(),
            Timestamp.valueOf(obs.getObsTime()),
            obs.getLocationLat(), obs.getLocationLon(),
            obs.getWeather(), obs.getSeeing(),
            obs.getMoonPhase() != null
                ? obs.getMoonPhase().name().toLowerCase() : null,
            obs.getNote(), obs.getObsId()}) > 0;
    }

    public boolean delete(int obsId) {
        // 级联清理三张关联表（FK 未定义 CASCADE 时显式删除；定义了则是纵深防御）
        executeUpdate("DELETE FROM obs_equipment WHERE obs_id = ?",
            new Object[]{obsId});
        executeUpdate("DELETE FROM obs_tag_relation WHERE obs_id = ?",
            new Object[]{obsId});
        // 删除观测记录本身
        return executeUpdate("DELETE FROM observations WHERE obs_id = ?",
            new Object[]{obsId}) > 0;
    }

    // === 多对多关联操作 ===

    // 插入一个观测-器材关联
    public void linkEquipment(int obsId, int equipId) {
        String sql = "INSERT IGNORE INTO obs_equipment (obs_id, equip_id) VALUES (?, ?)";
        executeUpdate(sql, new Object[]{obsId, equipId});
    }

    // 清除观测的所有器材关联（用于编辑时重建）
    public void unlinkAllEquipment(int obsId) {
        String sql = "DELETE FROM obs_equipment WHERE obs_id = ?";
        executeUpdate(sql, new Object[]{obsId});
    }

    // 查询观测关联的器材 ID 列表
    public List<Integer> getLinkedEquipmentIds(int obsId) {
        String sql = "SELECT equip_id FROM obs_equipment WHERE obs_id = ?";
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, obsId);
            rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("equip_id"));
        } catch (SQLException e) {
            System.err.println("查询关联器材失败: " + e.getMessage());
        } finally { closeResources(rs, stmt, conn); }
        return ids;
    }

    // 插入一个观测-标签关联
    public void linkTag(int obsId, int tagId) {
        String sql = "INSERT IGNORE INTO obs_tag_relation (obs_id, tag_id) VALUES (?, ?)";
        executeUpdate(sql, new Object[]{obsId, tagId});
    }

    // 清除观测的所有标签关联
    public void unlinkAllTags(int obsId) {
        String sql = "DELETE FROM obs_tag_relation WHERE obs_id = ?";
        executeUpdate(sql, new Object[]{obsId});
    }

    // 查询观测关联的标签 ID 列表
    public List<Integer> getLinkedTagIds(int obsId) {
        String sql = "SELECT tag_id FROM obs_tag_relation WHERE obs_id = ?";
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, obsId);
            rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("tag_id"));
        } catch (SQLException e) {
            System.err.println("查询关联标签失败: " + e.getMessage());
        } finally { closeResources(rs, stmt, conn); }
        return ids;
    }

    // === 组合条件查询（动态 SQL） ===

    public List<Observation> search(int userId, LocalDateTime startTime,
                                     LocalDateTime endTime, Integer bodyId,
                                     Integer siteId, String weather,
                                     Integer seeing, String moonPhase,
                                     String keyword) {
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT o.obs_id, o.user_id, o.body_id, o.site_id, "
          + "o.obs_time, o.location_lat, o.location_lon, o.weather, "
          + "o.seeing, o.moon_phase, o.note, o.create_time "
          + "FROM observations o "
          + "LEFT JOIN obs_tag_relation otr ON o.obs_id = otr.obs_id "
          + "LEFT JOIN observation_tags ot ON otr.tag_id = ot.tag_id "
          + "WHERE o.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startTime != null) {
            sql.append(" AND o.obs_time >= ?");
            params.add(Timestamp.valueOf(startTime));
        }
        if (endTime != null) {
            sql.append(" AND o.obs_time <= ?");
            params.add(Timestamp.valueOf(endTime));
        }
        if (bodyId != null) {
            sql.append(" AND o.body_id = ?");
            params.add(bodyId);
        }
        if (siteId != null) {
            sql.append(" AND o.site_id = ?");
            params.add(siteId);
        }
        if (weather != null && !weather.isEmpty()) {
            sql.append(" AND o.weather = ?");
            params.add(weather);
        }
        if (seeing != null) {
            sql.append(" AND o.seeing = ?");
            params.add(seeing);
        }
        if (moonPhase != null && !moonPhase.isEmpty()) {
            sql.append(" AND o.moon_phase = ?");
            params.add(moonPhase.toLowerCase());
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (o.note LIKE ? OR ot.name LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        sql.append(" ORDER BY o.obs_time DESC");
        return executeQuery(sql.toString(), params.toArray(), this::mapRow);
    }

    // === ResultSet 映射（package-private 可被 Service 或其他 DAO 复用） ===

    Observation mapRow(ResultSet rs) throws SQLException {
        Observation o = new Observation();
        o.setObsId(rs.getInt("obs_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setBodyId(rs.getInt("body_id"));
        int sid = rs.getInt("site_id");
        o.setSiteId(rs.wasNull() ? null : sid);
        Timestamp ot = rs.getTimestamp("obs_time");
        o.setObsTime(ot != null ? ot.toLocalDateTime() : null);
        o.setLocationLat(rs.getBigDecimal("location_lat"));
        o.setLocationLon(rs.getBigDecimal("location_lon"));
        o.setWeather(rs.getString("weather"));
        o.setSeeing(rs.getInt("seeing"));
        String mp = rs.getString("moon_phase");
        o.setMoonPhase(mp != null ? MoonPhase.fromString(mp) : null);
        o.setNote(rs.getString("note"));
        Timestamp ct = rs.getTimestamp("create_time");
        o.setCreateTime(ct != null ? ct.toLocalDateTime() : null);
        return o;
    }
}
```

**关键细节：**

- `search` 方法用 LEFT JOIN 支持按标签关键词搜索，`DISTINCT` 避免重复行；
- `linkEquipment`/`linkTag` 使用 `INSERT IGNORE` 防止重复插入；
- 编辑观测时先 `unlinkAll` 再逐个 `link`，实现全量替换策略；
- `delete` 显式清理三级关联表（obs_equipment + obs_tag_relation + observations）；
- `INSERT IGNORE` 要求 MySQL 表有唯一约束（复合主键已满足）。

### 3.2 TagDao — 标签字典数据访问

**文件：** `src/main/java/com/astrolog/dao/TagDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.ObservationTag;
import java.sql.*;
import java.util.List;

public class TagDao extends BaseDao<ObservationTag> {

    public List<ObservationTag> findAll() {
        String sql = "SELECT tag_id, name, color FROM observation_tags ORDER BY name";
        return executeQuery(sql, null, this::mapRow);
    }

    public ObservationTag findByName(String name) {
        String sql = "SELECT tag_id, name, color FROM observation_tags WHERE name = ?";
        List<ObservationTag> results = executeQuery(sql, new Object[]{name}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    // 如果标签不存在则创建，返回标签 ID（用于 AddObsDialog 输入新标签）
    public int getOrCreate(String name, String color) {
        ObservationTag existing = findByName(name);
        if (existing != null) return existing.getTagId();
        String sql = "INSERT INTO observation_tags (name, color) VALUES (?, ?)";
        return executeInsert(sql, new Object[]{name, color});
    }

    public boolean delete(int tagId) {
        String sql = "DELETE FROM observation_tags WHERE tag_id = ?";
        return executeUpdate(sql, new Object[]{tagId}) > 0;
    }

    // 查询某个观测关联的标签
    public List<ObservationTag> findByObsId(int obsId) {
        String sql = "SELECT ot.tag_id, ot.name, ot.color "
                   + "FROM observation_tags ot "
                   + "JOIN obs_tag_relation otr ON ot.tag_id = otr.tag_id "
                   + "WHERE otr.obs_id = ? ORDER BY ot.name";
        return executeQuery(sql, new Object[]{obsId}, this::mapRow);
    }

    private ObservationTag mapRow(ResultSet rs) throws SQLException {
        ObservationTag t = new ObservationTag();
        t.setTagId(rs.getInt("tag_id"));
        t.setName(rs.getString("name"));
        t.setColor(rs.getString("color"));
        return t;
    }
}
```

### 3.3 SiteDao — 观测地点数据访问

**文件：** `src/main/java/com/astrolog/dao/SiteDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.ObservationSite;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class SiteDao extends BaseDao<ObservationSite> {

    public ObservationSite findById(int siteId) {
        String sql = "SELECT site_id, user_id, name, latitude, longitude, "
                   + "altitude, bortle_scale, best_time "
                   + "FROM observation_sites WHERE site_id = ?";
        List<ObservationSite> results = executeQuery(sql, new Object[]{siteId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<ObservationSite> findAllByUserId(int userId) {
        String sql = "SELECT site_id, user_id, name, latitude, longitude, "
                   + "altitude, bortle_scale, best_time "
                   + "FROM observation_sites WHERE user_id = ? ORDER BY name";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public int insert(ObservationSite site) {
        String sql = "INSERT INTO observation_sites (user_id, name, latitude, "
                   + "longitude, altitude, bortle_scale, best_time) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            site.getUserId(), site.getName(),
            site.getLatitude(), site.getLongitude(),
            site.getAltitude(), site.getBortleScale(), site.getBestTime()});
    }

    public boolean update(ObservationSite site) { /* UPDATE WHERE site_id=? */ return false; }
    public boolean delete(int siteId) { /* DELETE WHERE site_id=? */ return false; }

    private ObservationSite mapRow(ResultSet rs) throws SQLException {
        ObservationSite s = new ObservationSite();
        s.setSiteId(rs.getInt("site_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setName(rs.getString("name"));
        s.setLatitude(rs.getBigDecimal("latitude"));
        s.setLongitude(rs.getBigDecimal("longitude"));
        s.setAltitude(rs.getInt("altitude"));
        s.setBortleScale(rs.getInt("bortle_scale"));
        s.setBestTime(rs.getString("best_time"));
        return s;
    }
}
```

### 3.4 ObsService — 观测业务逻辑（核心）

**文件：** `src/main/java/com/astrolog/service/ObsService.java`

```java
package com.astrolog.service;

import com.astrolog.dao.ObsDao;
import com.astrolog.dao.TagDao;
import com.astrolog.dao.SiteDao;
import com.astrolog.model.Observation;
import java.time.LocalDateTime;
import java.util.List;

public class ObsService {

    private final ObsDao obsDao;
    private final TagDao tagDao;
    private final SiteDao siteDao;

    public ObsService() {
        this.obsDao = new ObsDao();
        this.tagDao = new TagDao();
        this.siteDao = new SiteDao();
    }

    // package-private 测试构造器
    ObsService(ObsDao obsDao, TagDao tagDao, SiteDao siteDao) {
        this.obsDao = obsDao;
        this.tagDao = tagDao;
        this.siteDao = siteDao;
    }

    // ==================== 观测 CRUD ====================

    public ServiceResult addObservation(Observation obs,
                                         List<Integer> equipIds,
                                         List<String> tagNames) {
        // 1. 必填校验
        if (obs.getBodyId() <= 0) return ServiceResult.fail("请选择观测星体");
        if (obs.getObsTime() == null) return ServiceResult.fail("观测时间不能为空");
        if (obs.getObsTime().isAfter(LocalDateTime.now())) {
            return ServiceResult.fail("观测时间不能在未来");
        }
        if (obs.getSeeing() < 1 || obs.getSeeing() > 5) {
            return ServiceResult.fail("视宁度必须在 1-5 之间");
        }

        // 2. 插入观测记录
        int obsId = obsDao.insert(obs);
        if (obsId <= 0) return ServiceResult.fail("添加失败");

        // 3. 关联器材
        if (equipIds != null) {
            for (int equipId : equipIds) obsDao.linkEquipment(obsId, equipId);
        }

        // 4. 关联标签（自动创建新标签）
        if (tagNames != null) {
            for (String tagName : tagNames) {
                String trimmed = tagName.trim();
                if (trimmed.isEmpty()) continue;
                int tagId = tagDao.getOrCreate(trimmed, "#3366CC");
                obsDao.linkTag(obsId, tagId);
            }
        }

        return ServiceResult.success("观测记录已添加");
    }

    public ServiceResult updateObservation(Observation obs,
                                            List<Integer> equipIds,
                                            List<String> tagNames) {
        // 校验同 add → obsDao.update(obs)
        // 全量替换：unlinkAll → 逐个 link
        obsDao.unlinkAllEquipment(obs.getObsId());
        if (equipIds != null) {
            for (int eid : equipIds) obsDao.linkEquipment(obs.getObsId(), eid);
        }
        obsDao.unlinkAllTags(obs.getObsId());
        if (tagNames != null) {
            for (String tn : tagNames) {
                int tid = tagDao.getOrCreate(tn.trim(), "#3366CC");
                obsDao.linkTag(obs.getObsId(), tid);
            }
        }
        return ServiceResult.success("观测记录已更新");
    }

    public ServiceResult deleteObservation(int obsId, int userId) {
        Observation obs = obsDao.findById(obsId);
        if (obs == null) return ServiceResult.fail("记录不存在");
        if (obs.getUserId() != userId) return ServiceResult.fail("无权删除他人记录");
        return obsDao.delete(obsId)
            ? ServiceResult.success("已删除") : ServiceResult.fail("删除失败");
    }

    // ==================== 查询 ====================

    public Observation getObservation(int obsId) { return obsDao.findById(obsId); }
    public List<Observation> listByUser(int userId) { return obsDao.findAllByUserId(userId); }

    public List<Observation> search(int userId, LocalDateTime start, LocalDateTime end,
                                     Integer bodyId, Integer siteId, String weather,
                                     Integer seeing, String moonPhase, String keyword) {
        return obsDao.search(userId, start, end, bodyId, siteId, weather, seeing,
            moonPhase, keyword);
    }

    // ==================== 关联查询 ====================

    public List<Integer> getEquipmentIds(int obsId) { return obsDao.getLinkedEquipmentIds(obsId); }
    public List<String> getTagNames(int obsId) {
        return tagDao.findByObsId(obsId).stream()
            .map(t -> t.getName()).collect(Collectors.toList());
    }
}
```

### 3.5 SiteService — 观测地点业务逻辑

**文件：** `src/main/java/com/astrolog/service/SiteService.java`

```java
public class SiteService {
    private final SiteDao siteDao;
    // 公开 + 测试构造器
    // CRUD: addSite/updateSite/deleteSite/listByUser/getSite
    // 校验：name 必填、lat(-90~90)、lon(-180~180)、bortle(1~9)
}
```

### 3.6 ObservationPanel — 观测记录管理面板（UI 核心）

**文件：** `src/main/java/com/astrolog/ui/panel/ObservationPanel.java`

**布局结构（~380 行）：**

```
┌──────────────────────────────────────────────────────────────┐
│ BorderLayout                                                  │
│                                                              │
│ ┌─ 筛选栏（NORTH）─────────────────────────────────────────┐ │
│ │ 时间: [____] 至 [____]   星体: [搜索选择器]              │ │
│ │ 地点: [____▼]   天气: [____▼]   视宁度: [1-5▼]          │ │
│ │ 月相: [全部▼]   关键词: [______]  [搜索]  [清除]       │ │
│ └──────────────────────────────────────────────────────────┘ │
│                                                              │
│ ┌─ 数据表格（CENTER）──────────────────────────────────────┐ │
│ │ JTable (10列):                                           │ │
│ │ 日期 | 星体 | 星座 | 地点 | 天气 | 视宁度 | 月相 |      │ │
│ │ 器材 | 标签 | 笔记预览                                   │ │
│ └──────────────────────────────────────────────────────────┘ │
│                                                              │
│ ┌─ 操作栏（SOUTH）─────────────────────────────────────────┐ │
│ │ [添加观测] [编辑] [删除]                                  │ │
│ │ 共 N 条记录                                               │ │
│ └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public ObservationPanel(User currentUser)`
2. JTable 显示观测记录，10 列：`{"日期", "星体", "星座", "地点", "天气", "视宁度", "月相", "器材", "标签", "笔记"}`
3. 星体列显示 starName（通过 JOIN 查询或后台关联 BodyDao）；地点列显示 siteName（JOIN 或后台关联 SiteDao）
4. 器材列显示逗号分隔的设备名（后台 JOIN obs_equipment + equipment）
5. 标签列显示标签名，用对应颜色渲染（JLabel 设置前景色）
6. 筛选栏的"星体"使用可搜索下拉框（JTextField + 下拉建议列表），从 BodyDao.listAll() 获取
7. 时间范围用 JTextField（日期格式 yyyy-MM-dd HH:mm）或日期选择组件
8. "添加观测"弹出 AddObsDialog
9. "编辑"预填选中行数据并弹出 AddObsDialog（编辑模式）
10. "删除"弹出确认对话框，仅记录所有者可操作
11. 表格初始加载用户所有观测记录

**数据加载优化（避免 N+1）：**
每行关联的星体名/星座/器材名/标签名应在一次 SQL 中批量获取。推荐方案：扩展 ObsDao.search 返回的结果，或 ObservationPanel 中使用后台查询拼接。简单起见，S5 可在每行渲染时单独查询 BodyDao/EquipDao/TagDao（数据量小，N+1 影响有限）。但器材列和标签列应该通过 ObsDao.getLinkedEquipmentIds/ getLinkedTagIds + 批量查询来减少网络往返。

### 3.7 AddObsDialog — 添加/编辑观测对话框

**文件：** `src/main/java/com/astrolog/ui/dialog/AddObsDialog.java`

```
┌──────────────────────────────────────┐
│ 添加观测记录 / 编辑观测记录          │
├──────────────────────────────────────┤
│                                      │
│ 观测星体 *: [搜索选择器___________] │
│ 观测时间 *: [____-__-__ __:__]      │
│ 观测地点:   [_____▼] [新建地点]     │
│ 天气:       [_____▼]                │
│ 视宁度 *:   [1(极好) ▼]            │
│ 月相:       [全部 ▼]               │
│                                      │
│ 所用器材:                            │
│ ┌──────────────────────────────┐    │
│ │ JList (多选)                 │    │
│ │ 器材1, 器材2, 器材3, ...      │    │
│ └──────────────────────────────┘    │
│                                      │
│ 标签:                                │
│ 现有: [标签1] [标签2] [标签3] ...    │
│ 输入新标签: [________] [添加]        │
│                                      │
│ 观测笔记:                             │
│ ┌──────────────────────────────┐    │
│ │ JTextArea                     │    │
│ │                              │    │
│ └──────────────────────────────┘    │
│                                      │
│         [保存]    [取消]             │
└──────────────────────────────────────┘
```

**实现要点：**

1. 构造器：`public AddObsDialog(JFrame parent, User currentUser, Observation existing, Runnable onSaved)`
   - `existing == null` → 新增模式；`existing != null` → 编辑模式（预填所有字段）
2. 星体选择器：JTextField + JList 下拉建议（可搜索），数据来自 `new BodyDao().findAll()`；
   - 用户输入关键词时实时过滤列表（DocumentListener）
3. 观测时间：JTextField 初始为当前时间，格式 `yyyy-MM-dd HH:mm`
4. 观测地点：JComboBox + "新建地点"按钮弹出简易地点编辑对话框
5. 天气下拉：`["晴", "多云", "阴", "小雨", "雾", "大风"]` 等
6. 视宁度下拉：`["1(极好)", "2(好)", "3(一般)", "4(较差)", "5(差)"]`
7. 月相下拉：MoonPhase.values() 的 displayName 列表
8. 器材多选：JList(EquipDao.findAllByUserId 结果) + MULTIPLE_INTERVAL_SELECTION
9. 标签：上方显示已有标签按钮列表（TagDao.findAll），点击切换选中状态；下方输入框可创建新标签
10. 保存按钮事件：
    - 收集所有字段 → 构造 Observation + equipIds + tagNames
    - 新增模式调用 `obsService.addObservation()`；编辑模式调用 `obsService.updateObservation()`
    - 成功后 `onSaved.run()` → `dispose()`
    - 失败则 `JOptionPane.showMessageDialog` 显示错误
11. **【强制】构造器末尾调用 `setVisible(true)`**

---

## 4. MainFrame 修改

**文件：** `src/main/java/com/astrolog/ui/frame/MainFrame.java`

在第 2 个占位处理中添加：

```java
if (NAV_LABELS[i].equals("我的观测")) {
    ObservationPanel obsPanel = new ObservationPanel(currentUser);
    contentPanel.add(obsPanel, "我的观测");
}
```

---

## 5. 测试规格

### 5.1 ObsServiceTest（12 例）

```
@Mock ObsDao, @Mock TagDao, @Mock SiteDao; @InjectMocks ObsService

OS-001: addObservation_success()           — 正常添加（含器材+标签）
OS-002: addObservation_emptyBody()         — 空星体被拒
OS-003: addObservation_nullTime()          — 空时间被拒
OS-004: addObservation_futureTime()        — 未来时间被拒
OS-005: addObservation_invalidSeeing()     — 视宁度越界（0 或 6）
OS-006: updateObservation_success()        — 更新记录（器材标签全量替换）
OS-007: deleteObservation_ownRecord()      — 删除自己的记录
OS-008: deleteObservation_othersRecord()   — 删除他人记录被拒
OS-009: addObservation_withNewTag()        — 新标签自动创建
OS-010: search_byTimeRange()               — 按时间范围查询
OS-011: search_byBodyId()                  — 按星体查询
OS-012: search_byKeyword()                 — 按关键词查询（笔记+标签）
```

### 5.2 SiteServiceTest（3 例）

```
SS-001: 正常添加地点 → 成功
SS-002: 空名称 → 被拒
SS-003: 经纬度越界 → 被拒
```

### 5.3 ObsDaoTest（8 例）

```
@BeforeEach: 通过 UserDao + BodyDao 创建测试用户和星体

IT-OD-001: insert → findById → 所有字段值一致
IT-OD-002: insert → linkEquipment → getLinkedEquipmentIds → 正确
IT-OD-003: insert → linkTag → getLinkedTagIds → 正确
IT-OD-004: update → findById → 更新字段正确
IT-OD-005: delete → 三张关联表（obs_equipment/obs_tag_relation）无残留
IT-OD-006: search 按时间范围 → 筛选正确
IT-OD-007: search 按 bodyId → 筛选正确
IT-OD-008: search 按关键词 → 笔记+标签搜索
```

### 5.4 TagDaoTest（3 例）

```
IT-TD-001: getOrCreate(新标签) → 创建成功，返回 ID
IT-TD-002: getOrCreate(已存在) → 返回已有 ID
IT-TD-003: delete → 删除成功
```

### 5.5 SiteDaoTest（4 例）

```
IT-SD-001: insert → findById → 字段一致
IT-SD-002: insert → update → findById → 更新正确
IT-SD-003: delete → findById → null
IT-SD-004: insert 2 个 → findAllByUserId → 返回 2 条
```

---

## 6. 任务执行顺序

```
第 1 步 → ObsDao.java（依赖 BaseDao，核心 DAO 最先写）
第 2 步 → TagDao.java + SiteDao.java（依赖 BaseDao）
第 3 步 → ObsService.java（依赖 ObsDao+TagDao+SiteDao，保留测试构造器）
第 4 步 → SiteService.java（依赖 SiteDao，保留测试构造器）
第 5 步 → AddObsDialog.java（依赖 ObsService+BodyDao+EquipDao+TagDao+SiteDao）
第 6 步 → ObservationPanel.java（依赖 ObsService，可独立于 Dialog 先写表格）
第 7 步 → 修改 MainFrame.java（"我的观测" → ObservationPanel）
第 8 步 → ObsServiceTest.java + SiteServiceTest.java
第 9 步 → ObsDaoTest.java + TagDaoTest.java + SiteDaoTest.java
```

---

## 7. 验收标准

| #   | 验收项                                           | 验证方法                  |
| --- | ------------------------------------------------ | ------------------------- |
| 1   | `mvn clean compile` 零错误                       | Maven 编译                |
| 2   | `mvn test` 全部通过（73 + 30 = 103 例）          | 执行测试                  |
| 3   | 添加观测记录（含器材+标签+地点），表格刷新       | 手动操作                  |
| 4   | 编辑观测记录，器材/标签全量替换正确              | 手动操作                  |
| 5   | 删除自己的观测记录，确认对话框                   | 手动操作                  |
| 6   | 无法删除他人记录                                 | 切换用户测试              |
| 7   | 按时间/星体/地点/天气/视宁度/月相/关键词组合筛选 | 逐个+组合测试             |
| 8   | 新标签自动创建并关联                             | AddObsDialog 输入新标签名 |
| 9   | 多重筛选 + 标签筛选正确                          | 按标签 + 时间组合         |
| 10  | 观测记录表格显示星体名/星座/器材名/标签名        | 手动查看表格内容          |

---

## 8. 注意事项

1. **obs_equipment / obs_tag_relation 无自增主键：** 这两个关联表使用复合主键，不可用 executeInsert（会返回 -1）。全部使用 executeUpdate + INSERT IGNORE。
2. **编辑策略是全量替换：** 编辑观测时不逐个对比差异，统一 `unlinkAll + 重新 link`。实现简单且数据量小。
3. **siteId 可为 null：** Observation.siteId 是 nullable Integer。ObsDao.mapRow 中使用 `rs.getInt` + `rs.wasNull()` 判断。
4. **moonPhase DB 存储小写：** MoonPhase.fromString() 已支持 name() 和 displayName 双向匹配。
5. **避免跨层调用：** ObservationPanel 不直接调用 DAO，所有数据访问通过 ObsService。但星体下拉和器材多选的数据源可以实例化 BodyDao/EquipDao（简单的下拉数据获取，非业务逻辑）。
6. **Dialog 必须 setVisible(true)：** 这是 S3 以来反复出现的 Bug，AddObsDialog 构造器末尾必须调用。
7. **时间校验：** obsTime 不能在未来（精确到分钟，允许当前时间），ObsService.addObservation 中校验。
8. **SELECT \* 禁用：** ObsDao 的 search 方法使用了 LEFT JOIN + DISTINCT，必须显式列出所有列名避免列歧义。
9. **N+1 容忍度：** 器材列和标签列在 100 条以内记录时可接受 N+1 查询。S6 可视化阶段会涉及更多数据，届时再优化。
10. **测试数据准备：** ObsDaoTest 需要先创建 User + Body + Site 作为 FK 依赖（可通过已有的 UserDao/BodyDao/SiteDao 辅助创建）。
