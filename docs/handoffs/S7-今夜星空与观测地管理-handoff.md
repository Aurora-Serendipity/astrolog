# S7 — 今夜星空与观测地管理 交接文档

> **子阶段：** S7 今夜星空与观测地管理
> **所属阶段：** 阶段三 — 扩展功能与收尾
> **编制日期：** 2026-05-27
> **上级会话状态：** S6 已完成（113/113 测试通过，G1-G4 审核通过），阶段二全部完成，阶段三启动

---

## 1. 前置状态速览

### 1.1 已交付资产

| 资产                   | 路径                            | S7 用途                                                                 |
| ---------------------- | ------------------------------- | ----------------------------------------------------------------------- |
| `SiteDao`              | S5 `dao/SiteDao.java`           | CRUD 已就绪，S7 的 ObservationSitePanel 直接复用                        |
| `SiteService`          | S5 `service/SiteService.java`   | addSite/updateSite/deleteSite/listByUser/getSite + 坐标/Bortle 校验     |
| `observation_sites` 表 | S5 sql/init.sql                 | site_id/user_id/name/latitude/longitude/altitude/bortle_scale/best_time |
| `ObservationSite` 实体 | S1 `model/ObservationSite.java` | 8 字段完整映射                                                          |
| `BodyDao`              | S3 `dao/BodyDao.java`           | NightSkyService 查询亮星数据                                            |
| `ObsDao`               | S5 `dao/ObsDao.java`            | NightSkyPanel "一键开始观测"跳转                                        |
| `DateUtil`             | S1 `util/DateUtil.java`         | calculateMoonPhase / isGoldenWindow（可能已实现空壳）                   |
| `MainFrame`            | `ui/frame/MainFrame.java`       | `getCurrentUserObject()` / `switchTo()`                                 |

### 1.2 MainFrame 当前状态

NAV_LABELS[4] = "观测地"，NAV_LABELS[5] = "今夜星空"，均为占位 JPanel，需替换。

### 1.3 S2-S6 全部偏差经验（S7 必须遵循）

| 经验                                       | S7 应用                                          |
| ------------------------------------------ | ------------------------------------------------ |
| 死代码清除（未使用字段/import）            | G1 重点检查 NightSkyService/ObservationSitePanel |
| UI 文件禁用通配符 import                   | 两个新 Panel                                     |
| Service 需 package-private 测试构造器      | NightSkyService(BodyDao)                         |
| N+1 避免                                   | NightSkyService 批量查询星体数据                 |
| JFreeChart 中文显示方框 → applyChineseFont | S7 不涉及 JFreeChart                             |
| 星座/星体数据考虑从 JSON 资源加载          | 星座季节/RA 范围数据硬编码在 NightSkyService 中  |
| 空数据友好提示                             | 观测地为空/今夜无可见星体时                      |
| 行数估算下调 ~30%（S6 偏差）               | 估算 ~1,200 → 实际可能 ~900                      |

---

## 2. S7 目标与范围

### 2.1 核心目标

实现 F8 观测地管理面板（UI）+ F11 今夜星空推荐算法与面板。

### 2.2 新增文件清单（6 个）

| #   | 文件                        | 包                      | 行数 |
| --- | --------------------------- | ----------------------- | ---- |
| 1   | `NightSkyService.java`      | `com.astrolog.service`  | ~220 |
| 2   | `ObservationSitePanel.java` | `com.astrolog.ui.panel` | ~260 |
| 3   | `NightSkyPanel.java`        | `com.astrolog.ui.panel` | ~280 |
| 4   | `NightSkyServiceTest.java`  | test `unit/service`     | ~130 |
| 5   | `SiteServiceTest.java`      | test `unit/service`     | ~70  |
| 6   | `SiteDaoTest.java`          | test `integration/dao`  | ~90  |

### 2.3 修改文件（1 个）

| #   | 文件             | 改动                                                        |
| --- | ---------------- | ----------------------------------------------------------- |
| 1   | `MainFrame.java` | "观测地" → ObservationSitePanel；"今夜星空" → NightSkyPanel |

### 2.4 范围边界

**不做：** 实时天气 API 集成（离线应用）、行星精确轨道计算（静态星座数据即可）、星图实时渲染（S6 StarMapCanvas 已完成星座画布，NightSkyPanel 引用它）。

---

## 3. 详细实现规格

### 3.1 NightSkyService — 今夜星空推荐算法

**文件：** `src/main/java/com/astrolog/service/NightSkyService.java`

**核心算法：**

```java
package com.astrolog.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.NightSkyData;
import com.astrolog.model.enums.MoonPhase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class NightSkyService {

    private final BodyDao bodyDao;

    // 星座数据：名称 → {minRA_h, maxRA_h, declination_deg, bestSeason}
    // 硬编码 12 个主要黄道/北天星座的可见赤经范围
    private static final Map<String, double[]> CONSTELLATION_RA = new LinkedHashMap<>();
    static {
        // 格式: {minRA_hours, maxRA_hours, midDec_deg}
        CONSTELLATION_RA.put("猎户座", new double[]{4.7, 6.4, 0});
        CONSTELLATION_RA.put("大熊座", new double[]{8.1, 14.5, 55});
        CONSTELLATION_RA.put("仙后座", new double[]{0.0, 3.5, 60});
        CONSTELLATION_RA.put("天鹅座", new double[]{19.2, 22.0, 40});
        CONSTELLATION_RA.put("天琴座", new double[]{18.2, 19.2, 36});
        CONSTELLATION_RA.put("天鹰座", new double[]{18.7, 20.6, 3});
        CONSTELLATION_RA.put("天蝎座", new double[]{15.8, 17.9, -30});
        CONSTELLATION_RA.put("金牛座", new double[]{3.4, 6.0, 17});
        CONSTELLATION_RA.put("双子座", new double[]{6.0, 8.1, 22});
        CONSTELLATION_RA.put("狮子座", new double[]{9.3, 11.8, 15});
        CONSTELLATION_RA.put("室女座", new double[]{11.6, 14.4, 0});
        CONSTELLATION_RA.put("牧夫座", new double[]{13.6, 15.8, 30});
    }

    // 公开构造器
    public NightSkyService() { this.bodyDao = new BodyDao(); }
    // package-private 测试构造器
    NightSkyService(BodyDao bodyDao) { this.bodyDao = bodyDao; }

    /**
     * 计算今夜可见推荐
     * @param date      观测日期
     * @param latitude  观测纬度（度）
     * @param longitude 观测经度（度）
     */
    public NightSkyData recommend(LocalDate date, double latitude, double longitude) {
        NightSkyData data = new NightSkyData();

        // 1. 计算月相
        data.setMoonPhase(calculateMoonPhase(date));

        // 2. 计算月升月落（简化算法，使用固定时间近似）
        data.setMoonRise(estimateMoonRise(date));
        data.setMoonSet(estimateMoonSet(date));

        // 3. 计算黄金观测窗口：日落后 1 小时 到 日落后 3 小时
        LocalTime sunset = estimateSunset(date, latitude, longitude);
        data.setSunsetTime(sunset);
        if (sunset != null) {
            data.setGoldenWindowStart(sunset.plusHours(1));
            data.setGoldenWindowEnd(sunset.plusHours(3));
        }

        // 4. 计算当地恒星时（Local Sidereal Time, LST）— 简化算法
        double lst = calculateLST(date, longitude);
        double visibleRAMin = (lst - 3 + 24) % 24;   // 子午线西侧 3 小时 = 已升起
        double visibleRAMax = (lst + 3) % 24;         // 子午线东侧 3 小时 = 即将升起

        // 5. 筛选可见星座
        List<String> visibleConstellations = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : CONSTELLATION_RA.entrySet()) {
            double[] range = entry.getValue();
            if (isRAInRange(range[0], range[1], visibleRAMin, visibleRAMax)
                    && isDecVisible(range[2], latitude)) {
                visibleConstellations.add(entry.getKey());
            }
        }
        data.setVisibleConstellations(visibleConstellations);

        // 6. 查询这些星座中的亮星（星等 < 3.0）
        List<CelestialBody> allBodies = bodyDao.findAll();
        List<CelestialBody> visibleStars = allBodies.stream()
            .filter(b -> b.getConstellation() != null
                && visibleConstellations.contains(b.getConstellation())
                && b.getType().getDisplayName().equals("恒星")
                && b.getMagnitude() != null
                && b.getMagnitude().compareTo(new BigDecimal("3.0")) < 0)
            .sorted(Comparator.comparing(CelestialBody::getMagnitude))
            .collect(Collectors.toList());
        data.setVisibleBodies(visibleStars);

        // 7. 梅西耶天体推荐
        List<CelestialBody> visibleMessier = allBodies.stream()
            .filter(b -> b.getConstellation() != null
                && visibleConstellations.contains(b.getConstellation())
                && b.getMessierNumber() != null)
            .sorted(Comparator.comparingInt(CelestialBody::getMessierNumber))
            .collect(Collectors.toList());
        data.setVisibleMessier(visibleMessier);

        return data;
    }

    // === 天文计算辅助（简化算法） ===

    private MoonPhase calculateMoonPhase(LocalDate date) {
        // 新月日基: 2000-01-06 为新月，农历周期 29.53 天
        LocalDate newMoonBase = LocalDate.of(2000, 1, 6);
        long days = newMoonBase.until(date).getDays();
        int phaseIndex = (int)(days % 2953 / 100.0 * 8) % 8; // 29.53天 → 8 相归一化
        return MoonPhase.values()[Math.abs(phaseIndex) % 8];
    }

    private LocalTime estimateSunset(LocalDate date, double lat, double lon) {
        // 极简日落估计算法（北半球中心纬度默认 18:00）
        int dayOfYear = date.getDayOfYear();
        // 夏至（6月）日落最晚 ~19:30，冬至（12月）日落最早 ~17:00
        double offset = -Math.cos(2 * Math.PI * (dayOfYear - 172) / 365) * 75; // 分钟偏移
        return LocalTime.of(18, 0).plusMinutes((long) offset);
    }

    private LocalTime estimateMoonRise(LocalDate date) {
        // 简化：月出约在日落后 0-3 小时之间，取决于月相
        // 新月 → 月亮在白天升起（不可见），满月 → 日落后立即升起
        MoonPhase phase = calculateMoonPhase(date);
        int phaseOrdinal = phase.ordinal(); // 0=新月, 4=满月
        int delayMinutes = (8 - Math.abs(phaseOrdinal - 4)) * 30;
        return LocalTime.of(17, 0).plusMinutes(delayMinutes);
    }

    private LocalTime estimateMoonSet(LocalDate date) {
        // 月落 ≈ 月出 + 12 小时
        LocalTime rise = estimateMoonRise(date);
        return rise.plusHours(12);
    }

    private double calculateLST(LocalDate date, double longitude) {
        // 格林尼治恒星时 (GST) 简化: 3 月春分日 0h UT → GST ≈ 12h
        LocalDate springEquinox = LocalDate.of(date.getYear(), 3, 21);
        long daysSinceEquinox = springEquinox.until(date).getDays();
        // 每天恒星时提前约 4 分钟 (= 0.0657 小时)
        double gst = (12.0 + daysSinceEquinox * 0.0657) % 24;
        // 本地恒星时 = GST + 经度/15
        return (gst + longitude / 15.0 + 24) % 24;
    }

    private boolean isRAInRange(double raMin, double raMax,
                                 double visibleMin, double visibleMax) {
        if (visibleMin <= visibleMax) {
            return !(raMax < visibleMin || raMin > visibleMax);
        } else {
            // 跨 0h 边界
            return !(raMax < visibleMin && raMin > visibleMax);
        }
    }

    private boolean isDecVisible(double dec, double lat) {
        // 简化：赤纬高过 (90° - 纬度) 才是拱极星，赤纬低于 -(90°-纬度) 永不升起
        // 大部分北天星座 dec > -40° 对中国可见
        return dec > -(90 - Math.abs(lat)) * 0.5;
    }
}
```

**关键设计：**

- 恒星时/月相/日落使用简化近似公式，非精确天文算法（桌面应用无需秒级精度）；
- 星座 RA 范围硬编码 12 个主要星座，扩展性好（后续可外置 JSON）；
- 亮星筛选星等 < 3.0（肉眼可见阈值）；
- 梅西耶天体筛选关联星座中已入库的。

### 3.2 NightSkyData 实体补充

**文件：** `src/main/java/com/astrolog/model/NightSkyData.java`（S1 已创建骨架，需确保包含以下字段）

```java
public class NightSkyData {
    private MoonPhase moonPhase;
    private LocalTime moonRise;
    private LocalTime moonSet;
    private LocalTime sunsetTime;
    private LocalTime goldenWindowStart;
    private LocalTime goldenWindowEnd;
    private List<String> visibleConstellations;
    private List<CelestialBody> visibleBodies;     // 亮星列表
    private List<CelestialBody> visibleMessier;     // 梅西耶天体列表
    // getter/setter + toString
}
```

如果 S1 的 NightSkyData 比较简单（只有几个 List 字段），在当前子阶段补充完整。

### 3.3 ObservationSitePanel — 观测地管理面板

**文件：** `src/main/java/com/astrolog/ui/panel/ObservationSitePanel.java`

**布局结构（~260 行）：**

```
┌──────────────────────────────────────────────┐
│ BorderLayout                                 │
│                                              │
│ ┌─ 地点列表（CENTER）──────────────────────┐ │
│ │ JTable (8列):                            │ │
│ │ 名称 | 纬度 | 经度 | 海拔 | Bortle |     │ │
│ │ 光害等级图示 | 最佳时段 | 观测次数       │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ ┌─ 操作栏（SOUTH）─────────────────────────┐ │
│ │ [添加地点] [编辑] [删除]                 │ │
│ └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public ObservationSitePanel(User currentUser)`
2. 复用 `SiteService`（S5 已交付）进行 CRUD
3. Bortle 光害等级列使用颜色渲染（1-3 暗色 → 7-9 亮色/红色渐变）
4. 观测次数通过 ObsDao 统计该地点被引用的次数
5. "添加/编辑"弹出 JDialog 表单：名称、纬度、经度、海拔(m)、Bortle 等级下拉(1-9)、最佳时段描述
6. 经纬度输入后做范围校验（SiteService 已实现）
7. Bortle 下拉旁显示简短说明（1=极暗, 5=郊区, 9=市中心）
8. 删除前弹出确认对话框
9. 表格初始加载 `siteService.listByUser(currentUser.getUserId())`

### 3.4 NightSkyPanel — 今夜星空面板

**文件：** `src/main/java/com/astrolog/ui/panel/NightSkyPanel.java`

**布局结构（~280 行）：**

```
┌──────────────────────────────────────────────────┐
│ BorderLayout                                     │
│                                                  │
│ ┌─ 输入区（NORTH）─────────────────────────────┐ │
│ │ 日期: [2026-05-27]  [今天] [昨天] [明天]     │ │
│ │ 位置: 纬度 [31.2] 经度 [121.5]               │ │
│ │       [使用默认坐标] [选择观测地▼]           │ │
│ │ [查询今夜星空]                                │ │
│ └───────────────────────────────────────────────┘ │
│                                                  │
│ ┌─ 推荐结果（CENTER）─ JTabbedPane ────────────┐ │
│ │                                               │ │
│ │ ┌─ 概览 ───────────────────────────────────┐ │ │
│ │ │ 月相图标 (🌑/🌓/🌕) + 名称               │ │ │
│ │ │ 日落: 18:45  |  黄金窗口: 19:45 - 21:45  │ │ │
│ │ │ 月出: 20:30  |  月落: 08:15              │ │ │
│ │ │ 可见星座: 猎户座, 金牛座, 双子座, ...    │ │ │
│ │ └──────────────────────────────────────────┘ │ │
│ │                                               │ │
│ │ ┌─ 亮星推荐 ───────────────────────────────┐ │ │
│ │ │ JTable: 名称 | 星座 | 视星等 | 类型      │ │ │
│ │ │ [开始观测] → 跳转到 AddObsDialog          │ │ │
│ │ └──────────────────────────────────────────┘ │ │
│ │                                               │ │
│ │ ┌─ 梅西耶天体 ─────────────────────────────┐ │ │
│ │ │ JTable: M# | 名称 | 类型 | 星座 | 星等   │ │ │
│ │ │ [开始观测] → 跳转到 AddObsDialog          │ │ │
│ │ └──────────────────────────────────────────┘ │ │
│ └───────────────────────────────────────────────┘ │
│                                                  │
│ ┌─ 状态栏（SOUTH）─────────────────────────────┐ │
│ │ 今晚可见 5 个星座，推荐 12 颗亮星，8 个梅西耶 │ │
│ └───────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public NightSkyPanel(User currentUser)`
2. 日期输入默认今天，"使用默认坐标"按钮从用户个人信息读取 lat/lon（`currentUser.getDefaultLat()`/`getDefaultLon()`）
3. "选择观测地"下拉从 `new SiteService().listByUser(userId)` 获取已有观测地
4. "查询今夜星空"按钮调用 `nightSkyService.recommend(date, lat, lon)` → 渲染结果
5. 月相使用 Unicode 符号或纯文字（🌑🌒🌓🌔🌕🌖🌗🌘，用 Font.SANS_SERIF 避免方框）
6. 亮星 JTable 和梅西耶 JTable 各有一个"开始观测"按钮或右键菜单 → 调用 `mainFrame.switchTo("我的观测")` 并触发 AddObsDialog 预填星体
7. 推荐结果为空时显示 "今晚可见条件不佳，建议选择其他日期或地点"
8. 需要传入 MainFrame 引用（用于跳转到观测面板并触发 AddObsDialog）

**NightSkyPanel 与 MainFrame 交互方案：**

```java
// 在 MainFrame 中创建 NightSkyPanel 时：
NightSkyPanel skyPanel = new NightSkyPanel(currentUser, this); // this = MainFrame

// NightSkyPanel 内部：
private void openAddObservation(CelestialBody body) {
    mainFrame.switchTo("我的观测");
    // 通过 SwingUtilities.invokeLater 延迟触发 AddObsDialog
    // 或 ObservationPanel 暴露 showAddDialog(CelestialBody preselectedBody) 方法
}
```

---

## 4. MainFrame 修改

```java
// NAV_LABELS[4] = "观测地"
if (NAV_LABELS[i].equals("观测地")) {
    ObservationSitePanel sitePanel = new ObservationSitePanel(currentUser);
    contentPanel.add(sitePanel, "观测地");
}

// NAV_LABELS[5] = "今夜星空"
if (NAV_LABELS[i].equals("今夜星空")) {
    NightSkyPanel skyPanel = new NightSkyPanel(currentUser, this);
    contentPanel.add(skyPanel, "今夜星空");
}
```

---

## 5. 测试规格

### 5.1 NightSkyServiceTest（8 例）

**文件：** `src/test/java/com/astrolog/unit/service/NightSkyServiceTest.java`

```
@Mock BodyDao; @InjectMocks NightSkyService

NS-001: recommend → 返回非 null NightSkyData
NS-002: recommend → 月相在 8 种有效值内
NS-003: recommend → 黄金窗口在日落后 1-3h 范围内
NS-004: recommend → 可见星座列表非空（给定合理坐标）
NS-005: recommend → 亮星已按星等升序排序
NS-006: recommend → 梅西耶天体按 M 编号升序
NS-007: calculateLST → 0h UT 在格林尼治 ≈ 12h（春分附近）
NS-008: calculateMoonPhase → 满月日（已知日期）返回 FULL_MOON
```

### 5.2 SiteService 扩展测试（4 例）

S5 未交付 SiteServiceTest（仅 SiteDaoTest）。S7 补充：

```
SS-001: addSite_success → 返回 success
SS-002: addSite_emptyName → 被拒
SS-003: addSite_invalidLat → 纬度越界被拒
SS-004: addSite_invalidBortle → Bortle 0 或 10 被拒
```

放在已有的测试目录 `test/unit/service/SiteServiceTest.java`。

---

## 6. 任务执行顺序

```
第 1 步 → NightSkyData 实体补充（确保字段完整）
第 2 步 → NightSkyService.java（依赖 BodyDao + NightSkyData + MoonPhase）
    注意：保留 package-private 测试构造器
第 3 步 → ObservationSitePanel.java（依赖 SiteService + SiteDao）
第 4 步 → NightSkyPanel.java（依赖 NightSkyService + BodyDao + MainFrame 引用）
第 5 步 → 修改 MainFrame.java（两个占位 → 两个面板）
第 6 步 → NightSkyServiceTest.java + SiteService 扩展测试
```

---

## 7. 验收标准

| #   | 验收项                                                    | 验证方法                         |
| --- | --------------------------------------------------------- | -------------------------------- |
| 1   | `mvn clean compile` 零错误                                | Maven 编译                       |
| 2   | `mvn test` 全部通过（113 + 12 = 125 例）                  | 执行测试                         |
| 3   | 观测地列表显示，添加/编辑/删除正常                        | 手动操作 ObservationSitePanel    |
| 4   | Bortle 等级列颜色渲染正确                                 | 视觉检查                         |
| 5   | 今夜星空：输入日期+坐标 → 查询 → 显示可见星座/亮星/梅西耶 | 手动操作 NightSkyPanel           |
| 6   | 月相 + 黄金观测窗口正确显示                               | 视觉检查（与实际数据对比）       |
| 7   | 亮星列表按星等排序                                        | 手动检查                         |
| 8   | "开始观测"按钮跳转到观测面板并预填星体                    | 手动点击                         |
| 9   | 空数据友好提示（无可见星座时）                            | 使用极端日期测试                 |
| 10  | 观测地可被 AddObsDialog 引用（S5 已实现）                 | 在添加观测时选择 S7 创建的观测地 |

---

## 8. 注意事项

1. **SiteDao/SiteService 已存在：** S5 已创建全功能 CRUD，S7 不需要重新实现。但 SiteServiceTest（单元测试）在 S5 未交付，S7 补充。
2. **天文计算精度可接受：** 月相/恒星时/日落使用简化近似公式。无需实现 VSOP87 或 ELP2000 级精密星表。误差在 ±30 分钟内对"推荐"场景足够。
3. **NightSkyData 实体检查：** S1 创建的 NightSkyData 骨架可能缺少 goldenWindowStart/goldenWindowEnd/sunsetTime 等字段，需补充。
4. **月相 Unicode：** 月相符号 🌑🌒🌓🌔🌕🌖🌗🌘 使用 Font.SANS_SERIF（S3 偏差经验），避免在 Windows Segoe UI Emoji 中出现方框。
5. **NightSkyPanel 需要 MainFrame 引用：** 用于"开始观测"跳转到观测面板。通过构造器注入 `(User, MainFrame)`。
6. **Bortle 翻译：** UI 显示"波特尔暗空等级"作为列标题，下拉 1-9 级，每级附加简短中文描述文本。
7. **星座数据硬编码：** 12 个主要星座的 RA 范围硬编码在 NightSkyService 中。后续可提取为 JSON 资源文件（`constellations_ra.json`），但 S7 不必须。
8. **DateUtil 可能已有空壳方法：** S1 的 DateUtil 可能已有 calculateMoonPhase/isGoldenWindow 空壳。S7 的 NightSkyService 独立实现，不依赖 DateUtil（避免 S1 工具类被强制实现）。
