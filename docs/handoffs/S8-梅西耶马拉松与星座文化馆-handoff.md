# S8 — 梅西耶马拉松与星座文化馆 交接文档

> **子阶段：** S8 梅西耶马拉松与星座文化馆
> **所属阶段：** 阶段三 — 扩展功能与收尾
> **编制日期：** 2026-05-27
> **上级会话状态：** S7 已完成（122/122 测试通过，G1-G4 审核通过）

---

## 1. 前置状态速览

### 1.1 已交付资产

| 资产                     | 路径                                 | S8 用途                                                                          |
| ------------------------ | ------------------------------------ | -------------------------------------------------------------------------------- |
| `MessierObject` 实体     | `model/MessierObject.java`           | JSON 数据容器，7 字段（M#/name/type/constellation/magnitude/season/description） |
| `ConstellationInfo` 实体 | `model/ConstellationInfo.java`       | JSON 数据容器，6 字段（name/abbreviation/area/brightestStar/season/mythology）   |
| `JsonDataLoader`         | `util/JsonDataLoader.java`           | `loadList(resourcePath, Class[])` 加载 JSON → 实体列表                           |
| `BodyDao`                | S3 `dao/BodyDao.java`                | `findByMessierNumber()` 或通过 search 获取星体 ID                                |
| `ObsDao`                 | S5 `dao/ObsDao.java`                 | `findAllByUserId(userId)` → 统计已观测的梅西耶天体                               |
| `CelestialBody`          | S1 `model/CelestialBody.java`        | messierNumber 字段（Integer, nullable）                                          |
| `BodyType`               | S1 `model/enums/BodyType.java`       | 星云/星团/星系 — 梅西耶天体类型分类                                              |
| `StarMapCanvas`          | S6 `ui/component/StarMapCanvas.java` | 星座画布，S8 的 ConstellationPanel 可嵌入复用                                    |
| `MainFrame`              | `ui/frame/MainFrame.java`            | `getCurrentUserObject()` / `switchTo()`                                          |

### 1.2 MainFrame 当前状态

NAV_LABELS[7] = "梅西耶马拉松"，NAV_LABELS[8] = "星座文化馆"，均为占位 JPanel，需替换。

### 1.3 S2-S7 全部偏差经验（S8 必须遵循）

| 经验               | S8 应用                                 |
| ------------------ | --------------------------------------- |
| 死代码清除         | G1 检查 JSON 加载后未使用变量           |
| 通配符 import 禁止 | MessierPanel / ConstellationPanel       |
| Service 测试构造器 | MessierService(BodyDao, ObsDao)         |
| JSON 加载容错      | JsonDataLoader 返回空列表时友好提示     |
| 空数据友好提示     | 无观测记录时进度条 0%，提示"开始观测吧" |
| 行数估算下调 ~25%  | 实际约 ~750 行 vs 交接估算 ~1,000 行    |

---

## 2. S8 目标与范围

### 2.1 核心目标

实现 F12 梅西耶天体全目录追踪（110 天体 + 进度 + 证书资格）+ F13 星座文化馆（88 星座中西文化数据 + 星图交互）。

### 2.2 新增文件清单（7 个）

| #   | 文件                        | 包                      | 行数        |
| --- | --------------------------- | ----------------------- | ----------- |
| 1   | `MessierService.java`       | `com.astrolog.service`  | ~160        |
| 2   | `ConstellationService.java` | `com.astrolog.service`  | ~100        |
| 3   | `MessierPanel.java`         | `com.astrolog.ui.panel` | ~250        |
| 4   | `ConstellationPanel.java`   | `com.astrolog.ui.panel` | ~220        |
| 5   | `MessierServiceTest.java`   | test `unit/service`     | ~90         |
| 6   | `messier_catalog.json`      | `resource/data/`        | ~300 行数据 |
| 7   | `constellations.json`       | `resource/data/`        | ~250 行数据 |

### 2.3 修改文件（1 个）

| #   | 文件             | 改动                                                             |
| --- | ---------------- | ---------------------------------------------------------------- |
| 1   | `MainFrame.java` | "梅西耶马拉松" → MessierPanel；"星座文化馆" → ConstellationPanel |

### 2.4 范围边界

**不做：** 梅西耶证书 PDF 生成（S9 ExportService 的 messier_cert.jrxml 模板）、88 星座全部连线（S6 StarMapCanvas 已有 15 星座）、梅西耶天体独立数据表（复用 observations + celestial_bodies 的 messier_number 字段）

---

## 3. 详细实现规格

### 3.1 JSON 资源文件

**文件：** `src/main/resources/data/messier_catalog.json`

```json
[
  {
    "messierNumber": 1,
    "name": "M1 蟹状星云",
    "type": "星云",
    "constellation": "金牛座",
    "magnitude": 8.4,
    "season": "冬",
    "description": "1054年超新星遗迹，梅西耶星表第一号天体"
  },
  {
    "messierNumber": 31,
    "name": "M31 仙女座星系",
    "type": "星系",
    "constellation": "仙女座",
    "magnitude": 3.44,
    "season": "秋",
    "description": "距银河系最近的巨型旋涡星系，肉眼可见"
  },
  {
    "messierNumber": 42,
    "name": "M42 猎户座大星云",
    "type": "星云",
    "constellation": "猎户座",
    "magnitude": 4.0,
    "season": "冬",
    "description": "最明亮的弥漫星云，冬季星空的标志性目标"
  },
  {
    "messierNumber": 45,
    "name": "M45 昴星团",
    "type": "星团",
    "constellation": "金牛座",
    "magnitude": 1.6,
    "season": "冬",
    "description": "著名的疏散星团，俗称七姐妹星团"
  }
]
```

**S8 需要包含全部 110 条数据**（由新对话中的 Claude 生成完整 JSON，包括 M1-M110）。

**文件：** `src/main/resources/data/constellations.json`

```json
[
  {
    "name": "猎户座",
    "abbreviation": "Ori",
    "area": 594,
    "brightestStar": "参宿七",
    "season": "冬",
    "mythology": "希腊神话中的猎人奥利安，被蝎子蜇死后升天为星座。中国古代星官体系中，猎户座对应参宿。"
  },
  {
    "name": "大熊座",
    "abbreviation": "UMa",
    "area": 1280,
    "brightestStar": "玉衡",
    "season": "春",
    "mythology": "希腊神话中卡利斯托被宙斯变为熊后升天。中国古代北斗七星属大熊座，是重要的天文导航星群。"
  }
]
```

**S8 需要包含全部 88 条**，每条包含中西方文化融合的 mythology 字段。

### 3.2 MessierService — 梅西耶追踪服务

**文件：** `src/main/java/com/astrolog/service/MessierService.java`

```java
package com.astrolog.service;

import com.astrolog.dao.ObsDao;
import com.astrolog.dao.BodyDao;
import com.astrolog.model.MessierObject;
import com.astrolog.model.Observation;
import com.astrolog.util.JsonDataLoader;
import java.util.*;
import java.util.stream.Collectors;

public class MessierService {

    private final BodyDao bodyDao;
    private final ObsDao obsDao;
    private final List<MessierObject> catalog;  // 启动时加载一次

    // 公开构造器
    public MessierService() {
        this.bodyDao = new BodyDao();
        this.obsDao = new ObsDao();
        this.catalog = JsonDataLoader.loadList(
            "data/messier_catalog.json", MessierObject[].class);
    }

    // package-private 测试构造器
    MessierService(BodyDao bodyDao, ObsDao obsDao) {
        this.bodyDao = bodyDao;
        this.obsDao = obsDao;
        this.catalog = new ArrayList<>(); // 测试用空数据
    }

    // ==================== 目录查询 ====================

    // 获取全部 110 天体
    public List<MessierObject> getFullCatalog() { return catalog; }

    // 按季节筛选
    public List<MessierObject> filterBySeason(String season) {
        return catalog.stream()
            .filter(m -> season == null || season.isEmpty()
                || m.getSeason().equals(season))
            .collect(Collectors.toList());
    }

    // 按类型筛选
    public List<MessierObject> filterByType(String type) {
        return catalog.stream()
            .filter(m -> type == null || type.isEmpty()
                || m.getType().equals(type))
            .collect(Collectors.toList());
    }

    // ==================== 追踪状态 ====================

    // 获取用户已观测的梅西耶天体编号集合
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

    // 判断某个梅西耶天体是否已被观测
    public boolean isObserved(int messierNumber, Set<Integer> observed) {
        return observed.contains(messierNumber);
    }

    // 计算完成进度百分比
    public double getProgress(Set<Integer> observed) {
        return (double) observed.size() / catalog.size() * 100.0;
    }

    // 判断是否满足证书资格（100% 完成）
    public boolean isCertEligible(Set<Integer> observed) {
        return observed.size() >= catalog.size();
    }

    // 获取按类型分组的完成统计：Map<类型, Map<总数, 已完成数>>
    public Map<String, int[]> getStatsByType(Set<Integer> observed) {
        Map<String, int[]> stats = new LinkedHashMap<>();
        for (MessierObject m : catalog) {
            stats.putIfAbsent(m.getType(), new int[]{0, 0});
            stats.get(m.getType())[0]++; // 总数
            if (observed.contains(m.getMessierNumber())) {
                stats.get(m.getType())[1]++; // 已完成
            }
        }
        return stats;
    }
}
```

### 3.3 ConstellationService — 星座文化服务

**文件：** `src/main/java/com/astrolog/service/ConstellationService.java`

```java
package com.astrolog.service;

import com.astrolog.model.ConstellationInfo;
import com.astrolog.util.JsonDataLoader;
import java.util.*;
import java.util.stream.Collectors;

public class ConstellationService {

    private final List<ConstellationInfo> constellations;

    public ConstellationService() {
        this.constellations = JsonDataLoader.loadList(
            "data/constellations.json", ConstellationInfo[].class);
    }

    // package-private 测试构造器
    ConstellationService(List<ConstellationInfo> testData) {
        this.constellations = testData;
    }

    public List<ConstellationInfo> getAll() { return constellations; }

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

    // 搜索（名称/缩写/亮星匹配）
    public List<ConstellationInfo> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) return constellations;
        return constellations.stream()
            .filter(c -> c.getName().contains(keyword)
                || c.getAbbreviation().toLowerCase().contains(
                    keyword.toLowerCase())
                || c.getBrightestStar().contains(keyword))
            .collect(Collectors.toList());
    }
}
```

### 3.4 MessierPanel — 梅西耶马拉松面板

**文件：** `src/main/java/com/astrolog/ui/panel/MessierPanel.java`

**布局结构（~250 行）：**

```
┌──────────────────────────────────────────────────┐
│ BorderLayout                                     │
│                                                  │
│ ┌─ 顶部（NORTH）───────────────────────────────┐ │
│ │ ┌─ 进度条区域 ──────────────────────────────┐ │ │
│ │ │ [══════════════ 45/110 (40.9%) ══════════] │ │ │
│ │ │ 星团: 12/29  星云: 5/7  星系: 28/74       │ │ │
│ │ │ [查看证书] ← 100% 完成时可用              │ │ │
│ │ └───────────────────────────────────────────┘ │ │
│ │ 筛选: 类型[全部▼] 季节[全部▼] 状态[全部▼]   │ │
│ └───────────────────────────────────────────────┘ │
│                                                  │
│ ┌─ 天体列表（CENTER）──────────────────────────┐ │
│ │ JTable (7列):                                │ │
│ │ M# | 名称 | 类型 | 星座 | 星等 | 季节 | 状态 │ │
│ │ 状态列: ✓(已观测, 绿色) / ○(未观测, 灰色)   │ │
│ │ 已观测行星标绿色背景                          │ │
│ └───────────────────────────────────────────────┘ │
│                                                  │
│ ┌─ 详情区（EAST 或 双击弹出）──────────────────┐ │
│ │ 选中的天体详情: 名称/描述/星等/星座/季节      │ │
│ │ [快速跳转到星体库]                            │ │
│ └───────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public MessierPanel(User currentUser)`
2. 初始化时：`MessierService` 加载 JSON + `getObservedNumbers(userId)` 获取已观测集合
3. 进度条使用 JProgressBar（0-110），显示百分比 + 分数
4. 分类统计显示各类型的进度条
5. JTable 7 列：`{"M#", "名称", "类型", "星座", "视星等", "最佳季节", "状态"}`
6. 状态列渲染：已观测 ✓ (绿色 Font.SANS_SERIF)，未观测 ○ (灰色)
7. 已观测行整体设浅绿色背景（TableCellRenderer）
8. 筛选：类型下拉（全部/星云/星团/星系）、季节下拉（全部/春/夏/秋/冬）、状态下拉（全部/已观测/未观测）
9. 双击行弹出详情 JDialog（显示完整 description）
10. "查看证书"按钮：当 `isCertEligible()` 为 true 时启用，点击后提示"证书将在报告导出功能中生成（S9）"
11. 表格初始按 M# 升序排序

### 3.5 ConstellationPanel — 星座文化馆面板

**文件：** `src/main/java/com/astrolog/ui/panel/ConstellationPanel.java`

**布局结构（~220 行）：**

```
┌──────────────────────────────────────────────────┐
│ BorderLayout                                     │
│                                                  │
│ ┌─ 搜索栏（NORTH）─────────────────────────────┐ │
│ │ 搜索: [________]  季节: [全部▼]              │ │
│ │ [列表视图] [网格视图]                         │ │
│ └───────────────────────────────────────────────┘ │
│                                                  │
│ ┌─ 星座列表/网格（CENTER）─────────────────────┐ │
│ │ 列表模式: JTable (5 列)                      │ │
│ │   星座 | 缩写 | 面积 | 最亮星 | 最佳季节     │ │
│ │                                              │ │
│ │ 网格模式: JPanel GridLayout (11×8)           │ │
│ │   每个星座一个小卡片                          │ │
│ └───────────────────────────────────────────────┘ │
│                                                  │
│ ┌─ 详情区（EAST 或 点击展开）──────────────────┐ │
│ │ ┌───────────────────────────────────────────┐ │ │
│ │ │ 猎户座 (Orion, Ori)                      │ │ │
│ │ │ 面积: 594 平方度 | 排名: 26              │ │ │
│ │ │ 最亮星: 参宿七 (Rigel, 0.13等)          │ │ │
│ │ │ 最佳观测: 冬季                           │ │ │
│ │ │                                           │ │ │
│ │ │ 【中国星官】参宿                          │ │ │
│ │ │ 【希腊神话】...                           │ │ │
│ │ │                                           │ │ │
│ │ │ [在星图中查看]                             │ │ │
│ │ └───────────────────────────────────────────┘ │ │
│ └───────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public ConstellationPanel(User currentUser)`
2. 初始化 `ConstellationService`（加载 constellations.json）
3. 默认列表模式，`[网格视图]` 切换按钮
4. JTable 5 列：`{"星座", "缩写", "面积(平方度)", "最亮星", "最佳季节"}`
5. 网格模式：每个 JPanel 卡片显示星座名 + 缩写 + 小图标（使用 Unicode 星座符号 ♈♉♊♋♌♍♎♏♐♑♒♓，但仅限黄道 12 宫）
6. 点击星座 → 右侧详情区更新
7. 详情区展示中英文名称/缩写/面积/亮星/神话
8. 神话文本使用 JTextArea（自动换行，只读），支持中文换行
9. `[在星图中查看]` 按钮 → 弹出新窗口嵌入 S6 的 StarMapCanvas，可选高亮该星座连线
10. 搜索支持名称/缩写的包含匹配

---

## 4. JSON 数据生成规范

### messier_catalog.json（110 条）

S8 的新对话需生成完整 110 条。数据来源：公开天文学数据（梅西耶星表）。
关键字段：

- `messierNumber`: 1-110
- `type`: "星云" / "星团" / "星系"
- `magnitude`: 视星等，BigDecimal
- `season`: 最佳观测季节 "春"/"夏"/"秋"/"冬"

### constellations.json（88 条）

S8 的新对话需生成完整 88 星座。关键：

- `mythology` 字段融合中西文化："希腊神话：xxx。中国星官：xxx。"

---

## 5. MainFrame 修改

```java
// NAV_LABELS[7] = "梅西耶马拉松"
if (NAV_LABELS[i].equals("梅西耶马拉松")) {
    MessierPanel messierPanel = new MessierPanel(currentUser);
    contentPanel.add(messierPanel, "梅西耶马拉松");
}

// NAV_LABELS[8] = "星座文化馆"
if (NAV_LABELS[i].equals("星座文化馆")) {
    ConstellationPanel constPanel = new ConstellationPanel(currentUser);
    contentPanel.add(constPanel, "星座文化馆");
}
```

---

## 6. 测试规格

### 6.1 MessierServiceTest（5 例）

**文件：** `src/test/java/com/astrolog/unit/service/MessierServiceTest.java`

```
@Mock BodyDao; @Mock ObsDao; @InjectMocks MessierService

MS-001: getFullCatalog → 返回 110 条（验证 JSON 加载完整）
MS-002: filterByType("星云") → 只返回星云类型（4 条: M1/M27/M57/M76/M97）
MS-003: filterBySeason("冬") → 只返回冬季天体
MS-004: getProgress({M1,M31,M42,M45}) → 4/110 ≈ 3.6%
MS-005: isCertEligible(110 observed) → true
```

### 6.2 不强制 ConstellationService 单元测试

ConstellationService 是纯数据中转（JSON 加载 + filter），复杂度过低，通过 G2 手动验收覆盖即可。

---

## 7. 任务执行顺序

```
第 1 步 → JSON 数据文件生成（messier_catalog.json + constellations.json）
    注意：110 条梅西耶 + 88 条星座，mythology 字段中西文化融合
第 2 步 → MessierService.java（依赖 BodyDao + ObsDao + JsonDataLoader）
    注意：保留 package-private 测试构造器
第 3 步 → ConstellationService.java（依赖 JsonDataLoader）
第 4 步 → MessierPanel.java（依赖 MessierService）
第 5 步 → ConstellationPanel.java（依赖 ConstellationService + StarMapCanvas）
第 6 步 → 修改 MainFrame.java（两个占位 → 两个面板）
第 7 步 → MessierServiceTest.java
```

---

## 8. 验收标准

| #   | 验收项                                       | 验证方法                     |
| --- | -------------------------------------------- | ---------------------------- |
| 1   | `mvn clean compile` 零错误                   | Maven 编译                   |
| 2   | `mvn test` 全部通过（122 + 5 = 127 例）      | 执行测试                     |
| 3   | 梅西耶目录 110 条全量显示                    | 排序浏览，验证数量           |
| 4   | 已观测天体正确标记为 ✓（绿色），未观测显示 ○ | 对比实际观测记录             |
| 5   | 进度条 + 百分比 + 分类统计正确               | 视觉检查 + 计算验证          |
| 6   | 按类型/季节/状态筛选正常                     | 逐项筛选测试                 |
| 7   | 88 星座全量显示（列表+网格）                 | 排序浏览验证                 |
| 8   | 点击星座 → 详情区显示中英名称+神话           | 逐个点击                     |
| 9   | 星座搜索功能正常                             | 输入关键词                   |
| 10  | 100% 完成后"查看证书"按钮可用                | 模拟全部完成（或手动改代码） |

---

## 9. 注意事项

1. **无需 MessierDao：** S8 不需要独立数据库表存储梅西耶追踪状态。已观测信息完全从 `observations JOIN celestial_bodies WHERE messier_number IS NOT NULL` 推导。数据模型更简洁。
2. **JSON 数据量：** 110 条梅西耶 + 88 条星座 = ~550 行 JSON。新对话中的 Claude 可基于公开天文数据生成（M1-M110 + 88 星座）。数据准确性要求中等（做"推荐"和"文化展示"，不是科学研究）。
3. **M01-M110 覆盖：** 梅西耶编号非连续（M102 有争议），生成时全部 110 条使用 1-110 顺序编号，备注 M102 争议。
4. **ConstellationInfo.mythology 中英混合：** 该字段以中文描述为主，融合中西方：`"希腊神话：xxx。中国星官：xxx。"`。前后端一致，不另建中国星官实体。
5. **MessierService 测试数据隔离：** 测试构造器中 catalog 为空列表（测试只验证逻辑，不验证数据加载）。完整 110 条数据加载通过 G2 验收。
6. **星座网格视图性能：** 88 个 JPanel 卡片在 GridLayout 中渲染，Swing 可承受。无需虚拟化。
7. **80-20 原则：** messier_catalog.json 的 110 条数据可批量生成框架，细节在 S9 补充。constellations.json 的 mythology 字段先填 20 个主要星座的详细神话，其余用简化版。
