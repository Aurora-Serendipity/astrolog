# S3 — 星体信息管理 交接文档

> **子阶段：** S3 星体信息管理
> **所属阶段：** 阶段二 — 功能切片迭代
> **编制日期：** 2026-05-14
> **上级会话状态：** S2 已完成（48 文件、27/27 测试通过），偏差已记录。S3 将是第一个业务功能模块。

---

## 1. 前置状态速览

### 1.1 S2 已交付的关键资产

| 资产                  | 路径                           | S3 用途                                                               |
| --------------------- | ------------------------------ | --------------------------------------------------------------------- |
| `celestial_bodies` 表 | `sql/init.sql`                 | 含全部字段，S3 直接读写                                               |
| `user_favorites` 表   | `sql/init.sql`                 | user_id + body_id 复合主键，S3 管理收藏                               |
| `CelestialBody` 实体  | `model/CelestialBody.java`     | 17 字段完整映射，S3 直接使用                                          |
| `UserFavorite` 实体   | `model/UserFavorite.java`      | user_id + body_id + create_time                                       |
| `BodyType` 枚举       | `model/enums/BodyType.java`    | STAR/PLANET/NEBULA/CLUSTER/GALAXY                                     |
| `BaseDao<T>`          | `dao/BaseDao.java`             | executeQuery/executeUpdate/executeInsert/dataExists                   |
| `MainFrame`           | `ui/frame/MainFrame.java`      | `getCurrentUserObject()` 返回当前 User；`switchTo(cardName)` 切换面板 |
| `DashboardPanel`      | `ui/panel/DashboardPanel.java` | 构造器 `(User, MainFrame)`，快捷入口可跳转                            |
| `UserRole`            | `model/enums/UserRole.java`    | ADMIN 可增删改，OBSERVER 只读+收藏                                    |

### 1.2 S3 需要修改的 MainFrame 代码位置

MainFrame 当前第 22-27 行定义占位文本：

```java
private static final String[] PLACEHOLDER_TEXTS = {
    "", "星体库 - 将在S3实现", "我的观测 - 将在S5实现", ...
};
```

导航按钮对应关系：`NAV_LABELS[1]` = "星体库" → 当前为占位 JPanel → S3 替换为 CelestialBodyPanel。

MainFrame 当前第 105 行 CardLayout 注册名为 "星体库"：

```java
contentPanel.add(placeholder, NAV_LABELS[i]);  // i=1 → "星体库"
```

`MainFrame.switchTo("星体库")` 可被 DashboardPanel 快捷入口调用来跳转到星体面板。

---

## 2. S3 目标与范围

### 2.1 一句话目标

实现完整星体库管理：管理员可增删改星体数据 + CSV 批量导入，所有用户可浏览/搜索/收藏，按热度排序。

### 2.2 新增文件清单（10 个）

| #   | 文件                      | 包                                  | 行数             |
| --- | ------------------------- | ----------------------------------- | ---------------- |
| 1   | `BodyDao.java`            | `com.astrolog.dao`                  | ~250             |
| 2   | `FavoriteDao.java`        | `com.astrolog.dao`                  | ~100             |
| 3   | `BodyService.java`        | `com.astrolog.service`              | ~300             |
| 4   | `CelestialBodyPanel.java` | `com.astrolog.ui.panel`             | ~350             |
| 5   | `ImportCsvDialog.java`    | `com.astrolog.ui.dialog`            | ~200             |
| 6   | `BodyServiceTest.java`    | test `com.astrolog.unit.service`    | ~200             |
| 7   | `BodyDaoTest.java`        | test `com.astrolog.integration.dao` | ~180             |
| 8   | `FavoriteDaoTest.java`    | test `com.astrolog.integration.dao` | ~90              |
| 9   | `BodyDaoIT.java`          | test `com.astrolog.integration.dao` | ~60              |
| 10  | `seed_bodies.sql`         | `sql/`                              | ~100（数据内容） |

### 2.3 修改文件清单（2 个）

| #   | 文件                  | 修改内容                                                                                          |
| --- | --------------------- | ------------------------------------------------------------------------------------------------- |
| 1   | `MainFrame.java`      | 导航"星体库"从占位面板改为 CelestialBodyPanel；导入新 package                                     |
| 2   | `DashboardPanel.java` | "星体库"快捷入口点击跳转到星体面板（已有框架，仅确保按钮事件调用 `mainFrame.switchTo("星体库")`） |

### 2.4 范围边界

**不做：** 星座分布图绘制（S6）、与观测记录关联（S5）、星体图片展示（S9）

---

## 3. 详细实现规格

### 3.1 BodyDao — 星体数据访问

**文件：** `src/main/java/com/astrolog/dao/BodyDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.CelestialBody;
import com.astrolog.model.enums.BodyType;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BodyDao extends BaseDao<CelestialBody> {

    // === 基础 CRUD ===

    public CelestialBody findById(int bodyId) {
        String sql = "SELECT body_id, name, type, constellation, ra_h, ra_m, "
                   + "dec_deg, dec_min, magnitude, distance_ly, messier_number, "
                   + "ngc_number, best_season, description, image_path "
                   + "FROM celestial_bodies WHERE body_id = ?";
        List<CelestialBody> results = executeQuery(sql, new Object[]{bodyId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<CelestialBody> findAll() {
        String sql = "SELECT body_id, name, type, constellation, ra_h, ra_m, "
                   + "dec_deg, dec_min, magnitude, distance_ly, messier_number, "
                   + "ngc_number, best_season, description, image_path "
                   + "FROM celestial_bodies ORDER BY name";
        return executeQuery(sql, null, this::mapRow);
    }

    public int insert(CelestialBody body) {
        String sql = "INSERT INTO celestial_bodies (name, type, constellation, "
                   + "ra_h, ra_m, dec_deg, dec_min, magnitude, distance_ly, "
                   + "messier_number, ngc_number, best_season, description, image_path) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            body.getName(),
            body.getType().name().toLowerCase(),
            body.getConstellation(),
            body.getRaH(), body.getRaM(),
            body.getDecDeg(), body.getDecMin(),
            body.getMagnitude(), body.getDistanceLy(),
            body.getMessierNumber(), body.getNgcNumber(),
            body.getBestSeason(), body.getDescription(),
            body.getImagePath()});
    }

    public boolean update(CelestialBody body) {
        String sql = "UPDATE celestial_bodies SET name=?, type=?, constellation=?, "
                   + "ra_h=?, ra_m=?, dec_deg=?, dec_min=?, magnitude=?, "
                   + "distance_ly=?, messier_number=?, ngc_number=?, "
                   + "best_season=?, description=?, image_path=? "
                   + "WHERE body_id=?";
        return executeUpdate(sql, new Object[]{
            body.getName(),
            body.getType().name().toLowerCase(),
            body.getConstellation(),
            body.getRaH(), body.getRaM(),
            body.getDecDeg(), body.getDecMin(),
            body.getMagnitude(), body.getDistanceLy(),
            body.getMessierNumber(), body.getNgcNumber(),
            body.getBestSeason(), body.getDescription(),
            body.getImagePath(),
            body.getBodyId()}) > 0;
    }

    public boolean delete(int bodyId) {
        // 先删除关联的收藏记录（外键约束）
        String delFav = "DELETE FROM user_favorites WHERE body_id = ?";
        executeUpdate(delFav, new Object[]{bodyId});
        // 再删除星体
        String sql = "DELETE FROM celestial_bodies WHERE body_id = ?";
        return executeUpdate(sql, new Object[]{bodyId}) > 0;
    }

    // === 多条件动态查询 ===

    public List<CelestialBody> search(String constellation, String type,
                                       BigDecimal minMag, BigDecimal maxMag,
                                       String season, String keyword) {
        StringBuilder sql = new StringBuilder(
            "SELECT body_id, name, type, constellation, ra_h, ra_m, "
          + "dec_deg, dec_min, magnitude, distance_ly, messier_number, "
          + "ngc_number, best_season, description, image_path "
          + "FROM celestial_bodies WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (constellation != null && !constellation.isEmpty()) {
            sql.append(" AND constellation = ?");
            params.add(constellation);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type.toLowerCase());
        }
        if (minMag != null) {
            sql.append(" AND magnitude >= ?");
            params.add(minMag);
        }
        if (maxMag != null) {
            sql.append(" AND magnitude <= ?");
            params.add(maxMag);
        }
        if (season != null && !season.isEmpty()) {
            sql.append(" AND best_season = ?");
            params.add(season);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
        }

        sql.append(" ORDER BY name");
        return executeQuery(sql.toString(), params.toArray(), this::mapRow);
    }

    // === 收藏热度统计 ===

    public List<CelestialBody> findByPopularity() {
        String sql = "SELECT cb.body_id, cb.name, cb.type, cb.constellation, "
                   + "cb.ra_h, cb.ra_m, cb.dec_deg, cb.dec_min, cb.magnitude, "
                   + "cb.distance_ly, cb.messier_number, cb.ngc_number, "
                   + "cb.best_season, cb.description, cb.image_path, "
                   + "COUNT(uf.body_id) as fav_count "
                   + "FROM celestial_bodies cb "
                   + "LEFT JOIN user_favorites uf ON cb.body_id = uf.body_id "
                   + "GROUP BY cb.body_id "
                   + "ORDER BY fav_count DESC";
        return executeQuery(sql, null, rs -> {
            CelestialBody body = mapRow(rs);
            // fav_count 不在实体中，仅用于排序
            return body;
        });
    }

    // === ResultSet 映射 ===

    private CelestialBody mapRow(ResultSet rs) throws SQLException {
        CelestialBody b = new CelestialBody();
        b.setBodyId(rs.getInt("body_id"));
        b.setName(rs.getString("name"));
        b.setType(BodyType.fromString(rs.getString("type")));
        b.setConstellation(rs.getString("constellation"));
        b.setRaH(rs.getInt("ra_h"));
        b.setRaM(rs.getInt("ra_m"));
        b.setDecDeg(rs.getInt("dec_deg"));
        b.setDecMin(rs.getInt("dec_min"));
        b.setMagnitude(rs.getBigDecimal("magnitude"));
        b.setDistanceLy(rs.getBigDecimal("distance_ly"));
        // 可为 null 的 Integer 字段
        int mNum = rs.getInt("messier_number");
        b.setMessierNumber(rs.wasNull() ? null : mNum);
        int nNum = rs.getInt("ngc_number");
        b.setNgcNumber(rs.wasNull() ? null : nNum);
        b.setBestSeason(rs.getString("best_season"));
        b.setDescription(rs.getString("description"));
        b.setImagePath(rs.getString("image_path"));
        return b;
    }
}
```

**关键细节：**

- `search` 方法使用动态 SQL 拼接（`WHERE 1=1` + 条件附加），所有条件通过 PreparedStatement 参数化；
- `delete` 先清理 `user_favorites` 外键依赖再删星体；
- `messier_number` / `ngc_number` 为 nullable `Integer`，需用 `rs.wasNull()` 判断；
- `type` 字段存储小写，通过 `BodyType.fromString()` 转换为枚举。

### 3.2 FavoriteDao — 收藏数据访问

**文件：** `src/main/java/com/astrolog/dao/FavoriteDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.UserFavorite;
import com.astrolog.model.CelestialBody;
import java.sql.*;
import java.util.List;

public class FavoriteDao extends BaseDao<UserFavorite> {

    // 添加收藏（如果已存在则忽略）
    public boolean add(int userId, int bodyId) {
        if (exists(userId, bodyId)) return true;
        String sql = "INSERT INTO user_favorites (user_id, body_id) VALUES (?, ?)";
        return executeInsert(sql, new Object[]{userId, bodyId}) > 0;
    }

    // 取消收藏
    public boolean remove(int userId, int bodyId) {
        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND body_id = ?";
        return executeUpdate(sql, new Object[]{userId, bodyId}) > 0;
    }

    // 检查是否已收藏
    public boolean exists(int userId, int bodyId) {
        String sql = "SELECT COUNT(*) FROM user_favorites WHERE user_id = ? AND body_id = ?";
        List<UserFavorite> results = executeQuery(sql,
            new Object[]{userId, bodyId}, this::mapRow);
        return !results.isEmpty();
    }

    // 查询用户收藏的星体 ID 列表
    public List<Integer> findBodyIdsByUser(int userId) {
        String sql = "SELECT body_id FROM user_favorites WHERE user_id = ? "
                   + "ORDER BY create_time DESC";
        // 使用 BaseDao 查询并映射为 bodyId 列表
        // 简化：返回 UserFavorite 列表，Service 层提取 bodyId
        return null; // 替代方案见下方说明
    }

    // 查询用户收藏的完整星体列表
    public List<CelestialBody> findBodiesByUser(int userId) {
        String sql = "SELECT cb.body_id, cb.name, cb.type, cb.constellation, "
                   + "cb.ra_h, cb.ra_m, cb.dec_deg, cb.dec_min, cb.magnitude, "
                   + "cb.distance_ly, cb.messier_number, cb.ngc_number, "
                   + "cb.best_season, cb.description, cb.image_path "
                   + "FROM celestial_bodies cb "
                   + "INNER JOIN user_favorites uf ON cb.body_id = uf.body_id "
                   + "WHERE uf.user_id = ? "
                   + "ORDER BY uf.create_time DESC";
        // 此方法需要访问 CelestialBody 的 mapRow 逻辑
        // 可以在 FavoriteDao 中复制，或委托给 BodyDao
        // 推荐方案：FavoriteDao 中新建 BodyDao 实例进行委托
        return null; // 替代方案见下方说明
    }

    private UserFavorite mapRow(ResultSet rs) throws SQLException {
        UserFavorite f = new UserFavorite();
        // UserFavorite 实体字段：userId, bodyId, createTime
        f.setUserId(rs.getInt("user_id"));
        f.setBodyId(rs.getInt("body_id"));
        Timestamp ts = rs.getTimestamp("create_time");
        f.setCreateTime(ts != null ? ts.toLocalDateTime() : null);
        return f;
    }
}
```

**findBodiesByUser 的推荐实现方案（避免跨 DAO 代码重复）：**

在 FavoriteDao 中内联 BodyDao 的 mapRow 逻辑，或直接 new 一个 BodyDao 实例调用其公开方法。推荐后者——在 FavoriteDao 中添加：

```java
private final BodyDao bodyDao = new BodyDao();

public List<CelestialBody> findBodiesByUser(int userId) {
    String sql = "SELECT cb.* FROM celestial_bodies cb "
               + "INNER JOIN user_favorites uf ON cb.body_id = uf.body_id "
               + "WHERE uf.user_id = ? ORDER BY uf.create_time DESC";
    // 直接委托给 BodyDao 的公开查询能力
    // 方案：使用 bodyDao 提供的数据，或在此执行查询
    // 实际实现：执行 SQL，使用 bodyDao 的包级可见方法
    return null;  // 实现时参考 BodyDao.mapRow 模式
}
```

### 3.3 BodyService — 星体业务逻辑

**文件：** `src/main/java/com/astrolog/service/BodyService.java`

```java
package com.astrolog.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.FavoriteDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.enums.BodyType;
import com.astrolog.util.Validator;
import com.astrolog.util.ValidationResult;

import java.math.BigDecimal;
import java.util.List;

public class BodyService {

    private final BodyDao bodyDao;
    private final FavoriteDao favoriteDao;

    // 公开构造器（生产代码使用）
    public BodyService() {
        this.bodyDao = new BodyDao();
        this.favoriteDao = new FavoriteDao();
    }

    // package-private 测试构造器（供 Mockito @InjectMocks 使用）
    BodyService(BodyDao bodyDao, FavoriteDao favoriteDao) {
        this.bodyDao = bodyDao;
        this.favoriteDao = favoriteDao;
    }

    // ==================== CRUD ====================

    public ServiceResult addBody(CelestialBody body, int operatorUserId) {
        // 1. 校验必填字段
        if (body.getName() == null || body.getName().trim().isEmpty()) {
            return ServiceResult.fail("星体名称不能为空");
        }
        if (body.getType() == null) {
            return ServiceResult.fail("星体类型不能为空");
        }
        // 2. 坐标范围校验
        if (body.getRaH() < 0 || body.getRaH() > 23
                || body.getRaM() < 0 || body.getRaM() > 59) {
            return ServiceResult.fail("赤经格式无效（0-23h, 0-59m）");
        }
        if (body.getDecDeg() < -90 || body.getDecDeg() > 90
                || body.getDecMin() < 0 || body.getDecMin() > 59) {
            return ServiceResult.fail("赤纬格式无效（-90~90°, 0-59'）");
        }
        // 3. 插入
        int id = bodyDao.insert(body);
        if (id > 0) {
            return ServiceResult.success("星体已添加（ID: " + id + "）");
        }
        return ServiceResult.fail("添加失败，请重试");
    }

    public ServiceResult updateBody(CelestialBody body, int operatorUserId) {
        // 校验同上 → bodyDao.update(body)
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
        String[] lines = csvContent.split("\\R"); // 跨平台换行
        if (lines.length < 2) {
            result.addError(0, "CSV 至少需要标题行和一行数据");
            return result;
        }

        // 第一行为标题行，跳过
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            try {
                String[] cols = line.split(",", -1); // 保留空列
                if (cols.length < 5) {
                    result.addError(i, "列数不足（至少需要 name,type,constellation,ra,mag）");
                    continue;
                }

                CelestialBody body = new CelestialBody();
                body.setName(cols[0].trim());
                body.setType(BodyType.fromString(cols[1].trim()));
                body.setConstellation(cols[2].trim());

                // 解析赤经 "5h35m" 格式
                parseRA(cols[3].trim(), body);
                // 解析赤纬 "-5°23'" 格式
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
        // 格式: "5h35m" 或 "5.58"（十进制小时）
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
        // 格式: "-5°23'" 或 "-5.38"（十进制度）
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

// 导入结果类（包级独立文件或 BodyService 内部静态类）
public class ImportResult {
    private int successCount;
    private final List<String> errors = new ArrayList<>();
    // addSuccess / addError / getSuccessCount / getErrors / hasErrors
}
```

**关键点：**

- `ImportResult` 放在 `com.astrolog.service` 包中独立文件；
- CSV 解析支持两种坐标格式：`5h35m` / `5.58`（赤经），`-5°23'` / `-5.38`（赤纬）；
- `toggleFavorite` 自动判断添加/取消，前端只需一个收藏按钮。

### 3.4 CelestialBodyPanel — 星体管理面板

**文件：** `src/main/java/com/astrolog/ui/panel/CelestialBodyPanel.java`

**布局结构（~350 行）：**

```
┌─────────────────────────────────────────────┐
│ BorderLayout                                │
│                                             │
│ ┌─ 搜索栏（NORTH） ───────────────────────┐ │
│ │ 星座: [全部 ▼]  类型: [全部 ▼]         │ │
│ │ 亮度: [___] ~ [___]  季节: [全部 ▼]    │ │
│ │ 关键词: [_______________] [搜索]        │ │
│ │ [全部星体] [收藏排行] [我的收藏]        │ │
│ └─────────────────────────────────────────┘ │
│                                             │
│ ┌─ 数据表格（CENTER） ────────────────────┐ │
│ │ JTable (JScrollPane)                    │ │
│ │ 列: 名称|类型|星座|赤经|赤纬|星等|季节  │ │
│ │     |梅西耶#|NGC#|收藏                  │ │
│ └─────────────────────────────────────────┘ │
│                                             │
│ ┌─ 操作栏（SOUTH） ───────────────────────┐ │
│ │ [添加] [编辑] [删除] [导入CSV]          │ │
│ │ (编辑/删除仅选中行时可用)               │ │
│ │ (admin 所有按钮可见，observer 仅[导入]隐藏)│
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public CelestialBodyPanel(User currentUser)`
2. 搜索栏中星座下拉框动态从数据中提取不重复值（首次加载或每次刷新时更新）；
3. 类型下拉：`["全部", "恒星", "行星", "星云", "星团", "星系"]`；
4. 季节下拉：`["全部", "春", "夏", "秋", "冬"]`；
5. JTable 使用 `DefaultTableModel`，刷新时清空重填；
6. 收藏列显示 `★` / `☆`，点击切换收藏状态（行级按钮，使用 `TableCellEditor` 或独立的"收藏/取消"按钮）；
7. 双击行弹出详情对话框（只读，显示所有字段包括 description）；
8. "添加/编辑"弹出 JDialog 表单（名称、类型下拉、星座、赤经h、赤经m、赤纬°、赤纬'、星等、距离光年、梅西耶#、NGC#、最佳季节、描述）；
9. 权限控制：通过 `currentUser.getRole() == UserRole.ADMIN` 决定添加/编辑/删除/导入按钮的可见性；
10. 表格模型列定义：

```java
private static final String[] COLUMNS = {
    "名称", "类型", "星座", "赤经", "赤纬", "视星等", "最佳季节",
    "梅西耶#", "NGC#", "收藏"
};
```

### 3.5 ImportCsvDialog — CSV 导入对话框

**文件：** `src/main/java/com/astrolog/ui/dialog/ImportCsvDialog.java`

**布局（~200 行）：**

```
┌─────────────────────────────────────┐
│ 批量导入星体数据                     │
│                                     │
│ 文件: [________________] [浏览...] │
│                                     │
│ ┌─ 预览区（JTable）───────────────┐ │
│ │ 行号 | 名称 | 类型 | 星座 | ... │ │
│ │  1   | ...  | ...  | ...  |     │ │
│ │  2   | ...  | ...  | ...  |     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─ 导入结果 ──────────────────────┐ │
│ │ 成功: 15 条  失败: 2 条         │ │
│ │ ⚠ 第3行: 赤经格式无效          │ │
│ │ ⚠ 第7行: 列数不足              │ │
│ └─────────────────────────────────┘ │
│                                     │
│         [开始导入]  [关闭]          │
└─────────────────────────────────────┘
```

**实现要点：**

1. 构造器：`public ImportCsvDialog(JFrame parent, BodyService bodyService, int operatorUserId)`
2. "浏览"按钮使用 `JFileChooser` + `FileNameExtensionFilter("CSV 文件", "csv")`；
3. 文件选择后，用 `Files.readString()` 读取内容并在预览表中解析显示；
4. "开始导入"调用 `bodyService.importCsv(content, userId)`；
5. 结果区域显示成功/失败统计，失败原因逐行列出；
6. 导入成功后刷新父窗口的 CelestialBodyPanel 表格（通过回调或直接 `((MainFrame)parent).getContentPanel()` 查找）。

### 3.6 MainFrame 修改

**文件：** `src/main/java/com/astrolog/ui/frame/MainFrame.java`

**修改点（2 处）：**

**① 导入新包：**

```java
import com.astrolog.ui.panel.CelestialBodyPanel;
```

**② 将第 101-108 行的占位循环中的"星体库"条目替换：**

原代码（第 101-108 行）：

```java
for (int i = 1; i < NAV_LABELS.length; i++) {
    JPanel placeholder = new JPanel(new GridBagLayout());
    // ...
    contentPanel.add(placeholder, NAV_LABELS[i]);
}
```

改为在循环前先创建 CelestialBodyPanel：

```java
// 在 DashboardPanel 之后（第 99 行后）添加：
CelestialBodyPanel bodyPanel = new CelestialBodyPanel(currentUser);
contentPanel.add(bodyPanel, "星体库");

// 然后修改循环，跳过 i=1（星体库已手动添加）：
for (int i = 2; i < NAV_LABELS.length; i++) {  // i 从 2 开始
    JPanel placeholder = new JPanel(new GridBagLayout());
    // ... 同上
}
```

或者更简洁的方式——在循环内加判断：

```java
for (int i = 1; i < NAV_LABELS.length; i++) {
    if (NAV_LABELS[i].equals("星体库")) {
        CelestialBodyPanel bodyPanel = new CelestialBodyPanel(currentUser);
        contentPanel.add(bodyPanel, "星体库");
    } else {
        JPanel placeholder = new JPanel(new GridBagLayout());
        // ...
    }
}
```

### 3.7 DashboardPanel 修改

**文件：** `src/main/java/com/astrolog/ui/panel/DashboardPanel.java`

S2 的 DashboardPanel 已有快捷入口卡片区域和 `mainFrame.switchTo()` 方法。S3 只需确认：

- "星体库"快捷入口卡片点击事件调用 `mainFrame.switchTo("星体库")`；
- 摘要区星体数量从静态 "0" 改为从 BodyService 查询（可选，也可留到 S5 统一改为动态）。

### 3.8 种子数据

**文件：** `sql/seed_bodies.sql`

提供 20+ 经典星体的 INSERT 语句，涵盖梅西耶天体（如 M31 仙女座星系、M42 猎户座大星云）、亮星（天狼星、织女星）、行星（金星、木星）等。作为开发测试的初始数据。

示例：

```sql
INSERT INTO celestial_bodies (name, type, constellation, ra_h, ra_m, dec_deg, dec_min, magnitude, distance_ly, messier_number, best_season, description)
VALUES
('M31 仙女座星系', '星系', '仙女座', 0, 42, 41, 16, 3.44, 2537000, 31, '秋', '距离最近的大型旋涡星系，肉眼可见的模糊光斑'),
('M42 猎户座大星云', '星云', '猎户座', 5, 35, -5, 23, 4.0, 1344, 42, '冬', '最明亮的弥漫星云，冬季星空的标志性目标'),
('M45 昴星团', '星团', '金牛座', 3, 47, 24, 7, 1.6, 444, 45, '冬', '著名的疏散星团，俗称七姐妹星团'),
('天狼星', '恒星', '大犬座', 6, 45, -16, 43, -1.46, 8.6, NULL, '冬', '全天最亮的恒星'),
('织女星', '恒星', '天琴座', 18, 37, 38, 47, 0.03, 25, NULL, '夏', '夏季大三角之一，北天第二亮星');
```

完整的 20+ 条数据由新对话中的 Claude 自行生成。

---

## 4. 测试规格

### 4.1 BodyServiceTest（10 例）

**文件：** `src/test/java/com/astrolog/unit/service/BodyServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class BodyServiceTest {
    @Mock private BodyDao bodyDao;
    @Mock private FavoriteDao favoriteDao;
    @InjectMocks private BodyService bodyService;

    // BS-001: 正常添加星体
    @Test void addBody_success() { ... }

    // BS-002: 名称为空被拒
    @Test void addBody_emptyName() { ... }

    // BS-003: 赤经越界被拒
    @Test void addBody_invalidRA() { ... }

    // BS-004: 赤纬越界被拒
    @Test void addBody_invalidDec() { ... }

    // BS-005: 正常更新成功
    @Test void updateBody_success() { ... }

    // BS-006: 删除星体（含关联收藏清理）
    @Test void deleteBody_withFavorites() { ... }

    // BS-007: 收藏切换（添加 → 取消）
    @Test void toggleFavorite_addThenRemove() { ... }

    // BS-008: 搜索多条件筛选
    @Test void search_multipleConditions() { ... }

    // BS-009: CSV 导入有效文件
    @Test void importCsv_validFile() { ... }

    // BS-010: CSV 导入格式错误
    @Test void importCsv_invalidFormat() { ... }
}
```

### 4.2 BodyDaoTest（8 例）

**文件：** `src/test/java/com/astrolog/integration/dao/BodyDaoTest.java`

```
@BeforeEach: DELETE FROM user_favorites WHERE body_id IN (SELECT body_id FROM celestial_bodies WHERE name LIKE 'TEST_%');
             DELETE FROM celestial_bodies WHERE name LIKE 'TEST_%'

IT-BD-001: insert → findById → 所有字段值一致（用 compareTo 比较 BigDecimal）
IT-BD-002: insert → update 改名称+星等 → findById → 更新正确
IT-BD-003: insert → delete → findById → null
IT-BD-004: insert 2 个不同星座 → search(constellation="x") → 只返回匹配项
IT-BD-005: insert 3 个不同类型 → search(type="star") → 只返回匹配类型
IT-BD-006: insert 不同星等 → search(minMag=2, maxMag=5) → 范围筛选正确
IT-BD-007: insert → search(keyword="M31") → 模糊搜索命中
IT-BD-008: delete 有收藏的星体 → 外键清理成功，user_favorites 中无残留
```

### 4.3 FavoriteDaoTest（3 例）

**文件：** `src/test/java/com/astrolog/integration/dao/FavoriteDaoTest.java`

```
@BeforeEach: 通过 UserDao + BodyDao 创建测试用户和测试星体
             DELETE 测试收藏数据

IT-FD-001: add(userId, bodyId) → exists true → 收藏列表包含该星体
IT-FD-002: add → remove → exists false
IT-FD-003: add 2 个 → findBodiesByUser → 返回 2 条，时间倒序
```

### 4.4 Favorite 逻辑测试（可合并到 BodyServiceTest 中，4 例）

在 BodyServiceTest 中涵盖：

- `isFavorited` 返回 true/false
- `getFavorites` 返回正确列表
- `toggleFavorite` 调用链正确

---

## 5. BodyType.fromString() 补充

S1 的 `BodyType` 枚举需要像 `UserRole` 一样添加 `fromString()` 方法：

```java
public static BodyType fromString(String s) {
    if (s == null) return STAR;
    for (BodyType t : values()) {
        if (t.name().equalsIgnoreCase(s) || t.displayName.equals(s)) {
            return t;
        }
    }
    return STAR;
}
```

---

## 6. 任务执行顺序

```
第 1 步 → BodyDao.java（依赖 BaseDao）
第 2 步 → FavoriteDao.java（依赖 BaseDao + BodyDao 委托）
第 3 步 → BodyService.java + ImportResult.java（依赖 BodyDao + FavoriteDao）
    注意：需同时创建 package-private 测试构造器
第 4 步 → CelestialBodyPanel.java（依赖 BodyService + UserRole 权限）
第 5 步 → ImportCsvDialog.java（依赖 BodyService.importCsv + JFileChooser）
第 6 步 → 修改 MainFrame.java（将"星体库"占位改为 CelestialBodyPanel）
第 7 步 → 修改 DashboardPanel.java（确认快捷入口跳转）
第 8 步 → BodyServiceTest.java（依赖 Mockito, 第 3 步完成后即可编写）
第 9 步 → BodyDaoTest.java + FavoriteDaoTest.java（依赖真实 MySQL）
第 10 步 → seed_bodies.sql（种子数据）
```

---

## 7. 验收标准

| #   | 验收项                                              | 验证方法          |
| --- | --------------------------------------------------- | ----------------- |
| 1   | `mvn clean compile` 零错误                          | Maven 编译        |
| 2   | `mvn test` 全部通过（S1 7 + S2 20 + S3 25 = 52 例） | 执行测试          |
| 3   | 星体列表正确显示（至少 20 条种子数据）              | 启动应用 → 星体库 |
| 4   | 星座/类型/亮度/季节/关键词筛选功能正常              | 手动组合筛选      |
| 5   | 管理员可添加/编辑/删除星体                          | admin 登录操作    |
| 6   | 普通用户无法看到添加/编辑/删除/导入按钮             | observer 登录验证 |
| 7   | CSV 文件导入成功（含格式错误行的处理）              | 准备测试 CSV      |
| 8   | 收藏/取消收藏星体                                   | 点击收藏列        |
| 9   | "我的收藏"和"收藏排行"切换正常                      | 点击切换按钮      |
| 10  | 双击行弹出详情对话框                                | 手动双击          |

---

## 8. 注意事项

1. **动态搜索 SQL 安全：** BodyDao.search 的参数全部通过 PreparedStatement 参数化，不要直接拼接用户输入值到 SQL。
2. **BigDecimal 测试比较：** 使用 `compareTo()` 而非 `equals()`，避免 MySQL DECIMAL(4,2) 精度差异。
3. **delete 外键处理：** celestial_bodies 被 user_favorites 引用，删除前需要先清理关联数据。
4. **CSV 编码：** 导入使用 UTF-8 编码，`Files.readString(path, StandardCharsets.UTF_8)`。
5. **权限控制：** UI 层通过 `UserRole.ADMIN` 控制操作按钮可见性，Service 层不重复校验角色（信任 UI 层 + 后续 S2 登录已确认角色）。
6. **收藏列渲染：** JTable 的"收藏"列可用 JButton 渲染器或 Unicode 符号 ★/☆，点击时通过 `MouseListener` 获取行列坐标判断。
7. **测试构造器：** BodyService 必须提供 package-private `BodyService(BodyDao, FavoriteDao)` 构造器，这是 S2 偏差经验——没有它 Mockito @InjectMocks 无法注入。
