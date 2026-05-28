# 架构设计文档 (SAD)

> **项目名称：** AstroLog — 天文观测日志与星体管理系统
> **版本：** v1.0
> **编制日期：** 2026-05-13
> **参考标准：** ISO/IEC 42010 软件架构描述

---

## 1. 引言

### 1.1 编写目的

本文档描述 AstroLog 系统的软件架构，包括架构决策、模块划分、层间交互、数据设计和部署方案。为开发团队提供统一的技术实现基准，确保系统的一致性、可维护性和可扩展性。

### 1.2 架构目标

- **分层隔离：** 表示层、业务层、数据层职责明确，上层不跨层调用
- **可测试性：** 每层可独立进行单元测试，Service 层可 Mock DAO 进行隔离测试
- **可扩展性：** 新增功能模块不影响现有代码，符合开闭原则
- **可维护性：** 代码结构清晰，设计模式合理，命名遵循规范

---

## 2. 架构概览

### 2.1 架构风格

采用**三层架构（Three-Layer Architecture）**，以 Java SE 17 + Swing + JDBC 实现：

```
┌─────────────────────────────────────────────────┐
│                  表示层 (ui)                      │
│  LoginFrame, MainFrame, 10 Panels, 4 Dialogs    │
│  StarMapCanvas, SkyCalendarHeatmap              │
├─────────────────────────────────────────────────┤
│                业务逻辑层 (service)                │
│  11 Service 类, Validator, 策略/工厂/观察者      │
├─────────────────────────────────────────────────┤
│                数据访问层 (dao)                    │
│  BaseDao (模板方法), 11 DAO 实现                  │
├─────────────────────────────────────────────────┤
│              MySQL 8.0 数据库                     │
│  11 张表, utf8mb4, InnoDB                       │
└─────────────────────────────────────────────────┘
```

**核心原则：**

- 表示层仅调用 Service 层，不直接访问 DAO 或数据库
- Service 层处理业务逻辑、事务管理、校验协调
- DAO 层封装 JDBC 操作，返回实体对象或集合
- 实体对象 (model) 可以在三层之间传递

### 2.2 技术选型

| 层面       | 技术                    | 版本       | 选型理由                  |
| ---------- | ----------------------- | ---------- | ------------------------- |
| 开发语言   | Java SE                 | 17 (LTS)   | 课程要求，长期支持版本    |
| 界面框架   | Swing                   | JDK 内置   | 课程要求，GUI 标准组件    |
| 图表库     | JFreeChart              | 1.5.4      | 成熟的 Java 图表库        |
| 数据库     | MySQL                   | 8.0        | 课程要求，关系型数据库    |
| 数据库连接 | JDBC                    | —          | 原生方式，无 ORM 学习成本 |
| 连接池     | 自研 BlockingQueue 实现 | —          | 轻量级，适合桌面应用规模  |
| 报表引擎   | JasperReports           | 6.20       | Java 生态主流报表工具     |
| 密码加密   | jBCrypt                 | 0.4        | 行业标准 BCrypt 算法      |
| JSON 解析  | Gson                    | 2.10       | 读取内置天文数据资源      |
| 构建工具   | Maven                   | 3.8+       | 依赖管理标准化            |
| 测试框架   | JUnit 5 + Mockito       | 5.10 / 5.7 | Java 测试标准组合         |

### 2.3 设计模式应用

| 设计模式       | 应用场景                                                       | 涉及类                          |
| -------------- | -------------------------------------------------------------- | ------------------------------- |
| **模板方法**   | BaseDao 定义 JDBC 操作模板（获取连接→执行→释放），子类实现映射 | BaseDao + 11 DAO                |
| **策略模式**   | 报告导出格式（HTML vs PDF）、图表类型切换                      | ExportService, ChartUtil        |
| **观察者模式** | 主题切换时通知所有面板刷新样式；数据变更时刷新图表             | ThemeManager + Panels           |
| **工厂模式**   | Service 对象创建管理、图表生成器创建                           | ServiceFactory, ChartFactory    |
| **单例模式**   | 数据库连接池、主题管理器、应用配置                             | DBUtil, ThemeManager, AppConfig |
| **装饰器模式** | 校验器链式组合（如必填→格式→范围校验）                         | Validator                       |
| **DAO 模式**   | 数据访问层统一封装                                             | 11 个 DAO 类                    |

---

## 3. 模块划分

### 3.1 包结构

```
com.astrolog
├── AppMain.java                         // 程序入口
│
├── config                               // 配置层
│   ├── AppConfig.java                   // 应用级常量（版本、默认坐标等）
│   ├── ThemeConfig.java                 // 三套主题配色方案
│   └── DBConfig.java                    // 数据库连接参数
│
├── model                                // 实体层
│   ├── User.java                        // 用户实体
│   ├── CelestialBody.java              // 星体实体
│   ├── Observation.java                // 观测记录实体
│   ├── Equipment.java                  // 器材实体
│   ├── ObservationSite.java            // 观测地点实体
│   ├── EquipmentMaintenance.java       // 器材维护记录实体
│   ├── ObservationTag.java             // 标签字典实体
│   ├── UserFavorite.java               // 用户收藏实体
│   ├── OperationLog.java               // 操作日志实体
│   ├── MessierObject.java              // 梅西耶天体（来自 JSON）
│   ├── ConstellationInfo.java          // 星座信息（来自 JSON）
│   ├── NightSkyData.java               // 今夜星空推荐结果
│   └── enums/                           // 枚举类型
│       ├── UserRole.java               // observer, admin
│       ├── BodyType.java               // 恒星, 行星, 星云, 星团, 星系
│       ├── EquipType.java              // 望远镜, 目镜, 相机, 其他
│       ├── EquipStatus.java            // 在用, 维修, 退役
│       ├── BortleScale.java            // 1-9 光害等级
│       └── MoonPhase.java              // 8 种月相
│
├── dao                                  // 数据访问层
│   ├── BaseDao.java                     // JDBC 模板方法基类
│   ├── UserDao.java                     // 用户数据访问
│   ├── BodyDao.java                     // 星体数据访问
│   ├── ObsDao.java                      // 观测记录数据访问
│   ├── EquipDao.java                    // 器材数据访问
│   ├── SiteDao.java                     // 观测地点数据访问
│   ├── MaintDao.java                    // 维护日志数据访问
│   ├── TagDao.java                      // 标签数据访问
│   ├── FavoriteDao.java                 // 收藏数据访问
│   ├── LogDao.java                      // 操作日志数据访问
│   └── MessierDao.java                  // 梅西耶追踪状态持久化
│
├── service                              // 业务逻辑层
│   ├── UserService.java                 // 注册/登录/个人信息/用户管理
│   ├── BodyService.java                 // 星体CRUD/批量导入/收藏
│   ├── ObsService.java                  // 观测CRUD/计划/标签管理
│   ├── EquipService.java                // 器材CRUD/维护/统计
│   ├── StatsService.java                // 统计分析与图表数据准备
│   ├── SiteService.java                 // 观测地管理
│   ├── ExportService.java               // 报告生成(HTML/PDF/Messier证书)
│   ├── NightSkyService.java             // 今夜星空推荐算法
│   ├── MessierService.java              // 梅西耶马拉松追踪
│   ├── ConstellationService.java        // 星座文化数据服务
│   └── BackupService.java               // 数据备份与恢复
│
├── ui                                   // 表示层
│   ├── frame/
│   │   ├── LoginFrame.java              // 登录/注册界面
│   │   └── MainFrame.java               // 主界面（导航+CardLayout+状态栏）
│   ├── panel/
│   │   ├── DashboardPanel.java          // 首页仪表盘（快捷入口+摘要）
│   │   ├── UserPanel.java               // 用户信息面板
│   │   ├── CelestialBodyPanel.java     // 星体管理面板
│   │   ├── ObservationPanel.java       // 观测记录面板
│   │   ├── EquipmentPanel.java         // 器材管理面板
│   │   ├── ObservationSitePanel.java   // 观测地管理面板
│   │   ├── StatsPanel.java             // 统计图表面板
│   │   ├── NightSkyPanel.java          // 今夜星空面板
│   │   ├── MessierPanel.java           // 梅西耶马拉松面板
│   │   └── ConstellationPanel.java     // 星座文化馆面板
│   ├── dialog/
│   │   ├── AddObsDialog.java           // 添加/编辑观测记录对话框
│   │   ├── ReportViewDialog.java       // 报告预览对话框
│   │   ├── EquipmentMaintDialog.java   // 器材维护记录对话框
│   │   └── BackupRestoreDialog.java    // 备份恢复对话框
│   └── component/
│       ├── StarMapCanvas.java           // 星座亮星分布图画布
│       ├── SkyCalendarHeatmap.java      // 观测日历热力图组件
│       └── ThemeManager.java            // 主题管理（观察者模式核心）
│
├── util                                 // 工具类
│   ├── DBUtil.java                      // 数据库连接池（单例）
│   ├── ChartUtil.java                   // JFreeChart 图表工厂
│   ├── Validator.java                   // 链式校验框架（装饰器模式）
│   ├── ExportUtil.java                  // JasperReports 封装
│   ├── PasswordUtil.java                // BCrypt 密码哈希工具
│   ├── JsonDataLoader.java              // JSON 资源文件加载器
│   └── DateUtil.java                    // 天文日期计算（月相判断等）
│
└── resource/                            // 资源文件
    ├── data/
    │   ├── constellations.json          // 88 星座完整数据
    │   ├── messier_catalog.json         // 110 梅西耶天体数据
    │   └── bright_stars.json            // 亮星数据
    ├── report/
    │   ├── annual_report.jrxml          // JasperReports 年度报告模板
    │   └── messier_cert.jrxml           // 梅西耶证书模板
    └── i18n/
        ├── messages_zh.properties       // 中文语言文件
        └── messages_en.properties       // 英文语言文件
```

### 3.2 模块依赖关系

```
ui ──依赖──> service ──依赖──> dao ──依赖──> model
  │               │               │
  └──依赖──────> model <──────────┘

util ──被所有层依赖──> (model, config)
resource ──被──> service (JSON 数据), util (i18n)
config ──被──> (所有层)
```

---

## 4. 数据库设计

### 4.1 ER 图

```
users (1) ────────< (N) observations (N) >──────── (1) celestial_bodies
  │                       │                              │
  │                       ├──(N) obs_tag_relation (M)─── │
  │                       │         │                    │
  │                       │        (M)                    │
  │                       │   observation_tags           │
  │                       │                              │
  │                       ├──(N:1) observation_sites     │
  │                       │                              │
  │                       └──(N) obs_equipment (M)───    │
  │                                        │             │
  ├──(1)───< (N) equipment (1) ──< (N) equipment_maintenance
  │            │
  ├──(1)───< (N) user_favorites >─── (1) celestial_bodies
  │
  └──(1)───< (N) operation_logs
```

### 4.2 表结构定义

#### users（用户表）

| 字段           | 类型                     | 约束                         | 说明         |
| -------------- | ------------------------ | ---------------------------- | ------------ |
| user_id        | INT                      | PK, AUTO_INCREMENT           | 用户 ID      |
| username       | VARCHAR(50)              | UNIQUE, NOT NULL             | 登录名       |
| password       | VARCHAR(255)             | NOT NULL                     | BCrypt 哈希  |
| role           | ENUM('observer','admin') | NOT NULL, DEFAULT 'observer' | 角色         |
| avatar_path    | VARCHAR(255)             |                              | 头像图片路径 |
| city           | VARCHAR(100)             |                              | 所在城市     |
| default_lat    | DECIMAL(9,6)             |                              | 默认观测纬度 |
| default_lon    | DECIMAL(9,6)             |                              | 默认观测经度 |
| login_attempts | TINYINT                  | DEFAULT 0                    | 连续失败次数 |
| locked_until   | DATETIME                 |                              | 锁定截止时间 |
| last_login     | DATETIME                 |                              | 最后登录时间 |
| create_time    | TIMESTAMP                | DEFAULT CURRENT_TIMESTAMP    | 注册时间     |

#### celestial_bodies（星体信息表）

| 字段           | 类型          | 约束               | 说明                     |
| -------------- | ------------- | ------------------ | ------------------------ |
| body_id        | INT           | PK, AUTO_INCREMENT | 星体 ID                  |
| name           | VARCHAR(100)  | NOT NULL           | 常用名称                 |
| type           | VARCHAR(20)   | NOT NULL           | 恒星/行星/星云/星团/星系 |
| constellation  | VARCHAR(50)   |                    | 所属星座                 |
| ra_h           | INT           |                    | 赤经（小时）             |
| ra_m           | INT           |                    | 赤经（分）               |
| dec_deg        | INT           |                    | 赤纬（度）               |
| dec_min        | INT           |                    | 赤纬（分）               |
| magnitude      | DECIMAL(4,2)  |                    | 视星等                   |
| distance_ly    | DECIMAL(10,2) |                    | 距离（光年）             |
| messier_number | INT           |                    | 梅西耶编号               |
| ngc_number     | INT           |                    | NGC 编号                 |
| best_season    | VARCHAR(20)   |                    | 最佳观测季节             |
| description    | TEXT          |                    | 简介                     |
| image_path     | VARCHAR(255)  |                    | 图片路径                 |

#### observations（观测记录表）

| 字段         | 类型         | 约束                             | 说明         |
| ------------ | ------------ | -------------------------------- | ------------ |
| obs_id       | INT          | PK, AUTO_INCREMENT               | 记录 ID      |
| user_id      | INT          | FK → users                       | 观测者       |
| body_id      | INT          | FK → celestial_bodies            | 观测对象     |
| site_id      | INT          | FK → observation_sites, NULLABLE | 观测地点     |
| obs_time     | DATETIME     | NOT NULL                         | 观测时间     |
| location_lat | DECIMAL(9,6) |                                  | 观测纬度     |
| location_lon | DECIMAL(9,6) |                                  | 观测经度     |
| weather      | VARCHAR(50)  |                                  | 天气状况     |
| seeing       | TINYINT      |                                  | 视宁度 (1-5) |
| moon_phase   | VARCHAR(20)  |                                  | 月相         |
| note         | TEXT         |                                  | 观测笔记     |
| create_time  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP        | 创建时间     |

#### equipment（器材表）

| 字段          | 类型         | 约束               | 说明                  |
| ------------- | ------------ | ------------------ | --------------------- |
| equip_id      | INT          | PK, AUTO_INCREMENT | 器材 ID               |
| user_id       | INT          | FK → users         | 所有者                |
| name          | VARCHAR(100) | NOT NULL           | 器材型号              |
| type          | VARCHAR(50)  |                    | 望远镜/目镜/相机/其他 |
| aperture      | DECIMAL(5,2) |                    | 口径 (mm)             |
| focal_length  | INT          |                    | 焦距 (mm)             |
| purchase_date | DATE         |                    | 购买日期              |
| status        | VARCHAR(20)  | DEFAULT '在用'     | 在用/维修/退役        |
| description   | TEXT         |                    | 描述                  |

#### obs_equipment（观测-器材关联表）

| 字段        | 类型               | 约束              | 说明     |
| ----------- | ------------------ | ----------------- | -------- |
| obs_id      | INT                | FK → observations | 观测记录 |
| equip_id    | INT                | FK → equipment    | 所用器材 |
| PRIMARY KEY | (obs_id, equip_id) |                   | 联合主键 |

#### observation_sites（观测地点表）

| 字段         | 类型         | 约束               | 说明             |
| ------------ | ------------ | ------------------ | ---------------- |
| site_id      | INT          | PK, AUTO_INCREMENT | 地点 ID          |
| user_id      | INT          | FK → users         | 创建者           |
| name         | VARCHAR(100) | NOT NULL           | 地点名称         |
| latitude     | DECIMAL(9,6) | NOT NULL           | 纬度             |
| longitude    | DECIMAL(9,6) | NOT NULL           | 经度             |
| altitude     | INT          |                    | 海拔 (m)         |
| bortle_scale | TINYINT      |                    | 光害等级 (1-9)   |
| best_time    | VARCHAR(100) |                    | 最佳观测时段描述 |

#### equipment_maintenance（器材维护日志表）

| 字段            | 类型          | 约束               | 说明         |
| --------------- | ------------- | ------------------ | ------------ |
| maint_id        | INT           | PK, AUTO_INCREMENT | 记录 ID      |
| equip_id        | INT           | FK → equipment     | 关联器材     |
| maint_date      | DATE          | NOT NULL           | 维护日期     |
| description     | TEXT          |                    | 维护描述     |
| cost            | DECIMAL(10,2) |                    | 费用         |
| next_maint_date | DATE          |                    | 下次维护日期 |

#### observation_tags（标签字典表）

| 字段   | 类型        | 约束               | 说明               |
| ------ | ----------- | ------------------ | ------------------ |
| tag_id | INT         | PK, AUTO_INCREMENT | 标签 ID            |
| name   | VARCHAR(50) | UNIQUE, NOT NULL   | 标签名称           |
| color  | VARCHAR(7)  |                    | 颜色代码 (#RRGGBB) |

#### obs_tag_relation（观测-标签关联表）

| 字段        | 类型             | 约束                  | 说明     |
| ----------- | ---------------- | --------------------- | -------- |
| obs_id      | INT              | FK → observations     | 观测记录 |
| tag_id      | INT              | FK → observation_tags | 标签     |
| PRIMARY KEY | (obs_id, tag_id) |                       | 联合主键 |

#### user_favorites（用户收藏表）

| 字段        | 类型               | 约束                      | 说明     |
| ----------- | ------------------ | ------------------------- | -------- |
| user_id     | INT                | FK → users                | 用户     |
| body_id     | INT                | FK → celestial_bodies     | 收藏星体 |
| create_time | TIMESTAMP          | DEFAULT CURRENT_TIMESTAMP | 收藏时间 |
| PRIMARY KEY | (user_id, body_id) |                           | 联合主键 |

#### operation_logs（操作日志表）

| 字段        | 类型        | 约束                      | 说明     |
| ----------- | ----------- | ------------------------- | -------- |
| log_id      | INT         | PK, AUTO_INCREMENT        | 日志 ID  |
| user_id     | INT         | FK → users                | 操作用户 |
| operation   | VARCHAR(50) | NOT NULL                  | 操作类型 |
| detail      | TEXT        |                           | 操作详情 |
| ip_address  | VARCHAR(45) |                           | 操作 IP  |
| create_time | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP | 操作时间 |

### 4.3 建表 SQL 脚本

完整 SQL 脚本存放于项目 `sql/init.sql`，核心要点：

- 所有表使用 `CREATE TABLE IF NOT EXISTS`，可重复执行
- 引擎统一使用 InnoDB，字符集 utf8mb4
- 外键约束统一使用 `ON DELETE CASCADE`（观测记录关联数据级联删除）
- 除自增主键外，合理建立索引（外键列、常用查询列）

---

## 5. 关键交互流程

### 5.1 用户登录流程

```
LoginFrame                    UserService              UserDao              DB
    │                             │                      │                   │
    │──login(user,pass)──────────>│                      │                   │
    │                             │──findByUsername()───>│                   │
    │                             │                      │──SELECT─────────>│
    │                             │                      │<──User obj───────│
    │                             │<──User───────────────│                   │
    │                             │                      │                   │
    │                             │─BCrypt.check(pass)                        │
    │                             │─checkLocked()                             │
    │                             │─updateLoginTime()──>│──UPDATE──────────>│
    │                             │─logOperation()─────>│──INSERT──────────>│
    │<──LoginResult───────────────│                      │                   │
```

### 5.2 添加观测记录流程

```
ObservationPanel   ObsService    ObsDao  BodyDao  EquipDao  TagDao    DB
    │                  │            │        │        │        │         │
    │─addObs(data)────>│            │        │        │        │         │
    │                  │─validate() │        │        │        │         │
    │                  │─getBody()──┴───────>│────────┴───────>│         │
    │                  │<──Body────────────────────────────────│         │
    │                  │─insertObs()────>│────────────────────────────>│
    │                  │<──obsId─────────│<───────────────────────────│
    │                  │─linkEquip()─────────────>│──────────────────>│
    │                  │─linkTags()────────────────────────>│───────>│
    │                  │─linkSite() │        │        │        │         │
    │<──success────────│            │        │        │        │         │
```

### 5.3 主题切换流程（观察者模式）

```
MainFrame                   ThemeManager           All Panels
   │                            │                      │
   │──switchTheme("暗色")───────>│                      │
   │                            │──notifyObservers()──>│
   │                            │                      │──setBackground(darkBg)
   │                            │                      │──setForeground(lightText)
   │                            │                      │──revalidate()
   │                            │                      │──repaint()
   │<──repaint()────────────────│                      │
```

---

## 6. 界面设计

### 6.1 主界面布局

```
┌─────────────────────────────────────────────────┐
│ 菜单栏: 文件 | 数据管理 | 统计分析 | 帮助 | 主题  │
├──────────┬──────────────────────────────────────┤
│          │                                      │
│ 导航面板  │         内容区域                      │
│          │        (CardLayout)                   │
│ ─────── │                                      │
│ 仪表盘   │    ┌──────────────────────────┐      │
│ 星体库   │    │                          │      │
│ 我的观测  │    │    当前激活的面板内容      │      │
│ 器材柜   │    │                          │      │
│ 观测地   │    │                          │      │
│ 今夜星空  │    └──────────────────────────┘      │
│ 统计图表  │                                      │
│ 梅西耶   │                                      │
│ 星座文化  │                                      │
│ 系统设置  │                                      │
│          │                                      │
├──────────┴──────────────────────────────────────┤
│ 状态栏: 当前用户: xxx | 角色: xxx | 主题: xxx      │
└─────────────────────────────────────────────────┘
```

### 6.2 各面板通用设计模式

每个功能面板遵循统一模板：

- **顶部工具栏：** 搜索/筛选条件 + 操作按钮（添加/刷新/导出）
- **中部数据区：** JTable 展示数据 + 滚动条
- **右侧操作区：** 选中行后的编辑/删除/详情按钮
- **底部状态行：** 记录总数/选中行信息

---

## 7. 安全设计

### 7.1 认证与授权

- 密码使用 BCrypt 加盐哈希，不可逆存储
- 连续 5 次登录失败，账号锁定 30 分钟
- 管理员功能界面入口仅 admin 角色可见

### 7.2 数据安全

- 所有 SQL 查询使用 PreparedStatement，杜绝 SQL 注入
- 操作日志记录敏感操作（删除、权限变更）
- 数据备份功能提供 SQL 导出，支持灾难恢复

### 7.3 输入安全

- Validator 校验链确保所有用户输入经过格式和范围检查
- CSV 批量导入对每一行进行完整校验后才写入数据库

---

## 8. 部署方案

### 8.1 运行环境

| 组件  | 要求                     |
| ----- | ------------------------ |
| JDK   | 17 或更高版本            |
| MySQL | 8.0 或更高版本           |
| 内存  | 建议 512 MB 以上可用内存 |
| 磁盘  | 200 MB 可用空间          |

### 8.2 部署步骤

1. 安装 JDK 17+ 和 MySQL 8.0
2. 执行 `sql/init.sql` 初始化数据库
3. 修改 `src/main/resources/db.properties` 中的数据库连接信息
4. 执行 `mvn clean package` 构建项目
5. 运行 `java -jar astrolog.jar` 启动应用

---

## 9. 附录

### 附录 A：技术债务与后续演进

- Swing 可考虑迁移至 JavaFX 以获得更好的现代化 UI
- JDBC 可考虑引入 MyBatis 减少 SQL 模板代码
- 可扩展为 C/S 架构或多用户网络版

### 附录 B：修订历史

| 版本 | 日期       | 修订人    | 修订说明               |
| ---- | ---------- | --------- | ---------------------- |
| v1.0 | 2026-05-13 | AI + 人工 | 初始版本，完整架构设计 |
