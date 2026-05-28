\# 天文观测日志与星体管理系统（AstroLog）设计文档

> \*\*适用课程\*\*：Java程序设计与AI辅助编程  
> \*\*技术栈\*\*：Java SE 17 + Swing + MySQL + JFreeChart  
> \*\*架构模式\*\*：三层架构（表示层 / 业务逻辑层 / 数据访问层）

\---

\## 一、项目概述与需求规格书

\### 1.1 项目背景

天文爱好者或社团在长期观星中，需要系统化管理观测记录、星体信息及器材设备。传统纸质或Excel记录难以进行关联查询与统计分析，因此开发一款桌面应用，实现观测日志的数字化管理。

\### 1.2 功能需求

| 编号 | 模块         | 功能描述                                                 |
| ---- | ------------ | -------------------------------------------------------- |
| F1   | 用户管理     | 注册（观测者/管理员）、登录、个人信息维护、密码重置      |
| F2   | 星体信息管理 | 星体数据的增删改查，按星座/类型/亮度等条件筛选           |
| F3   | 观测记录管理 | 增删改查观测记录，关联用户、星体、器材，支持组合条件查询 |
| F4   | 器材管理     | 望远镜、目镜等设备的增删改查，统计使用次数               |
| F5   | 数据可视化   | 观测数量统计柱状图、星体类型分布饼图、简易星座亮星分布图 |
| F6   | 报告导出     | 生成个人年度观测报告（简单文本或HTML格式）               |
| F7   | 数据校验     | 用户重名校验、坐标格式校验、密码强度校验等               |

\### 1.3 非功能需求

\- 基于 Java Swing 桌面应用，界面简洁直观
\- 数据库使用 MySQL，提供完整建表脚本
\- 系统采用三层架构，核心逻辑与界面分离
\- AI辅助生成代码需经过人工逻辑校验与深度优化

\---

\## 二、系统功能模块图

```

天文观测日志与星体管理系统
│
├─ 用户管理模块
│   ├─ 用户注册
│   ├─ 用户登录
│   ├─ 个人信息修改
│   └─ 密码重置
│
├─ 星体信息管理模块
│   ├─ 星体信息添加
│   ├─ 星体信息编辑
│   ├─ 星体信息删除
│   └─ 多条件查询（按星座、类型、亮度等）
│
├─ 观测记录管理模块
│   ├─ 添加观测记录（关联星体、用户、器材）
│   ├─ 修改/删除本人记录
│   ├─ 组合条件查询（时间范围、星体、地点等）
│   └─ 观测统计与可视化
│       ├─ 按年度/月份统计观测次数（柱状图）
│       └─ 观测星体类型分布（饼图）
│
├─ 器材管理模块
│   ├─ 器材添加/修改/删除
│   └─ 器材使用次数统计
│
├─ 可视化展示模块
│   ├─ 统计图表绘制（封装 JFreeChart）
│   └─ 星座分布图绘制（基于坐标画点线）
│
└─ 系统工具模块
├─ 数据校验与格式转换
└─ 报告导出（简单 HTML）

```

\---

\## 三、数据库表结构设计

\### ER 关系概述

\- 一个用户 → 多条观测记录（1:N）
\- 一个星体 → 被多次观测（1:N）
\- 一次观测 → 可使用多个器材（M:N，通过中间表关联）

\### 3.1 用户表 users

| 字段名      | 类型                     | 约束                      | 说明         |
| ----------- | ------------------------ | ------------------------- | ------------ |
| user_id     | INT                      | PK, AUTO_INCREMENT        | 用户ID       |
| username    | VARCHAR(50)              | UNIQUE, NOT NULL          | 登录名       |
| password    | VARCHAR(255)             | NOT NULL                  | 加密存储     |
| role        | ENUM('observer','admin') | NOT NULL                  | 角色         |
| city        | VARCHAR(100)             |                           | 所在城市     |
| default_lat | DECIMAL(9,6)             |                           | 默认观测纬度 |
| default_lon | DECIMAL(9,6)             |                           | 默认观测经度 |
| create_time | TIMESTAMP                | DEFAULT CURRENT_TIMESTAMP | 注册时间     |

\### 3.2 星体信息表 celestial_bodies

| 字段名        | 类型         | 约束               | 说明                     |
| ------------- | ------------ | ------------------ | ------------------------ |
| body_id       | INT          | PK, AUTO_INCREMENT | 星体ID                   |
| name          | VARCHAR(100) | NOT NULL           | 常用名称                 |
| type          | VARCHAR(20)  | NOT NULL           | 恒星/行星/星云/星团/星系 |
| constellation | VARCHAR(50)  |                    | 所属星座                 |
| ra_h          | INT          |                    | 赤经（小时）             |
| ra_m          | INT          |                    | 赤经（分）               |
| dec_deg       | INT          |                    | 赤纬（度，正负）         |
| dec_min       | INT          |                    | 赤纬（分）               |
| magnitude     | DECIMAL(4,2) |                    | 视星等                   |
| best_season   | VARCHAR(20)  |                    | 最佳观测季节             |
| description   | TEXT         |                    | 简介                     |
| image_path    | VARCHAR(255) |                    | 图片路径                 |

\### 3.3 观测记录表 observations

| 字段名       | 类型         | 约束                      | 说明          |
| ------------ | ------------ | ------------------------- | ------------- |
| obs_id       | INT          | PK, AUTO_INCREMENT        | 记录ID        |
| user_id      | INT          | FK(users)                 | 观测者        |
| body_id      | INT          | FK(celestial_bodies)      | 观测对象      |
| obs_time     | DATETIME     | NOT NULL                  | 观测时间      |
| location_lat | DECIMAL(9,6) |                           | 观测纬度      |
| location_lon | DECIMAL(9,6) |                           | 观测经度      |
| weather      | VARCHAR(50)  |                           | 天气状况      |
| seeing       | TINYINT      |                           | 视宁度（1-5） |
| note         | TEXT         |                           | 观测笔记      |
| create_time  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | 记录创建时间  |

\### 3.4 器材表 equipment

| 字段名       | 类型         | 约束               | 说明                  |
| ------------ | ------------ | ------------------ | --------------------- |
| equip_id     | INT          | PK, AUTO_INCREMENT | 器材ID                |
| user_id      | INT          | FK(users)          | 所有者                |
| name         | VARCHAR(100) | NOT NULL           | 器材型号              |
| type         | VARCHAR(50)  |                    | 望远镜/目镜/相机/其他 |
| aperture     | DECIMAL(5,2) |                    | 口径(mm)              |
| focal_length | INT          |                    | 焦距(mm)              |
| description  | TEXT         |                    | 描述                  |

\### 3.5 观测-器材关联表 obs_equipment

| 字段名      | 类型               | 约束             | 说明     |
| ----------- | ------------------ | ---------------- | -------- |
| obs_id      | INT                | FK(observations) | 观测记录 |
| equip_id    | INT                | FK(equipment)    | 所用器材 |
| PRIMARY KEY | (obs_id, equip_id) |                  | 联合主键 |

\---

\## 四、系统用例描述

\### 4.1 参与者

\- \*\*普通用户（观测者）\*\*：管理个人观测记录与器材，查看星体库，生成统计报告
\- \*\*管理员\*\*：拥有普通用户全部权限，额外可维护星体数据、管理用户账号

\### 4.2 用例列表

| 用例编号 | 用例名称     | 参与者   | 简要描述                     |
| -------- | ------------ | -------- | ---------------------------- |
| UC01     | 用户注册     | 所有用户 | 填写信息注册账号，选择角色   |
| UC02     | 用户登录     | 所有用户 | 验证身份后进入主界面         |
| UC03     | 管理个人信息 | 所有用户 | 修改城市、默认坐标等         |
| UC04     | 星体数据维护 | 管理员   | 增删改星体信息               |
| UC05     | 浏览星体库   | 所有用户 | 按条件查询星体数据           |
| UC06     | 管理个人器材 | 观测者   | 增删改自己的器材             |
| UC07     | 添加观测记录 | 观测者   | 填写观测详情并关联星体与器材 |
| UC08     | 查询观测记录 | 观测者   | 按时间/星体/地点组合查询     |
| UC09     | 查看统计图表 | 观测者   | 生成观测数量柱状图与类型饼图 |
| UC10     | 导出观测报告 | 观测者   | 导出个人年度观测总结         |
| UC11     | 用户账号管理 | 管理员   | 查看/启用/禁用用户           |

\---

\## 五、系统架构设计

\### 5.1 技术选型

| 层面     | 技术                    | 说明                             |
| -------- | ----------------------- | -------------------------------- |
| 开发语言 | Java SE 17              | 核心开发语言                     |
| IDE      | IntelliJ IDEA + Trae AI | AI辅助编码与本地IDE集成          |
| 界面框架 | Java Swing              | 标准组件 + JFreeChart 图表库     |
| 数据库   | MySQL 8.0               | 通过 JDBC 连接                   |
| 构建工具 | Maven（可选）           | 管理 JFreeChart 等依赖           |
| 架构模式 | 三层架构                | 表示层 / 业务逻辑层 / 数据访问层 |

\### 5.2 包结构设计

```
com.astrolog
├── ui                          // 表示层 (Swing 界面)
│   ├── LoginFrame.java         // 登录界面
│   ├── MainFrame.java          // 主界面（含导航与卡片布局）
│   ├── panel                   // 各功能面板
│   │   ├── UserPanel.java      // 用户信息面板
│   │   ├── CelestialBodyPanel.java  // 星体管理面板
│   │   ├── ObservationPanel.java    // 观测记录面板
│   │   ├── EquipmentPanel.java      // 器材管理面板
│   │   └── ChartPanel.java          // 统计图表面板
│   └── dialog                  // 弹窗对话框
│       ├── AddObsDialog.java   // 添加观测记录对话框
│       └── ReportViewDialog.java    // 报告查看对话框
│
├── service                     // 业务逻辑层
│   ├── UserService.java        // 用户业务逻辑
│   ├── BodyService.java        // 星体业务逻辑
│   ├── ObsService.java         // 观测记录业务逻辑
│   ├── EquipService.java       // 器材业务逻辑
│   └── StatsService.java       // 统计分析业务逻辑
│
├── dao                         // 数据访问层
│   ├── BaseDao.java            // JDBC 基础工具（连接、关闭资源）
│   ├── UserDao.java            // 用户数据访问
│   ├── BodyDao.java            // 星体数据访问
│   ├── ObsDao.java             // 观测记录数据访问
│   └── EquipDao.java           // 器材数据访问
│
├── model                       // 实体类
│   ├── User.java               // 用户实体
│   ├── CelestialBody.java      // 星体实体
│   ├── Observation.java        // 观测记录实体
│   └── Equipment.java          // 器材实体
│
├── util                        // 工具类
│   ├── DBUtil.java             // 数据库连接池工具
│   ├── ChartUtil.java          // JFreeChart 图表生成工具
│   ├── Validator.java          // 数据校验工具
│   └── ExportUtil.java         // 报告导出工具
│
└── AppMain.java                // 程序入口（main方法）

```

\### 5.3 层间调用关系

```

表示层 (ui)
│ 调用
▼
业务逻辑层 (service)
│ 调用
▼
数据访问层 (dao)
│ 调用
▼
数据库 (MySQL)

```

\- 表示层只与业务逻辑层交互，不直接访问DA
\- 业务逻辑层处理校验、计算、事务协调
\- 数据访问层封装JDBC操作，返回实体对象

\---

\## 六、用户界面初步构思

\### 6.1 登录/注册界面
\- 简约深色背景（模拟星空），居中登录面板
\- 输入用户名/密码，登录按钮，注册跳转链接
\- 注册界面提供：用户名、密码、确认密码、角色选择、默认城市、坐标输入

\### 6.2 主界面布局

\- \*\*顶部菜单栏\*\*：文件（退出）、数据管理、统计分析、帮助
\- \*\*左侧导航区\*\*：垂直按钮列表（用户中心、星体库、我的观测、器材柜、统计图表）
\- \*\*右侧内容区\*\*：使用 CardLayout 切换显示对应功能面板
\- \*\*底部状态栏\*\*：显示当前登录用户、时间

\### 6.3 各功能面板设计要点

| 面板     | 布局                   | 核心组件                                 |
| -------- | ---------------------- | ---------------------------------------- |
| 用户中心 | 表单布局               | 个人信息文本字段、修改/保存按钮          |
| 星体库   | 上搜索 + 下表 + 右按钮 | 星座下拉/类型筛选/JTable/增删改按钮      |
| 我的观测 | 上筛选 + 下表 + 右按钮 | 时间范围选择器/JTable/添加/修改/删除按钮 |
| 器材柜   | 上表 + 下按钮          | JTable/添加/修改/删除按钮                |
| 统计图表 | 选项卡面板             | 年度频次柱状图/类型饼图/星座分布图       |

\### 6.4 星座分布图设计

\- 继承 JPanel 重写 paintComponent 方法
\- 以北极星为中心建立简易二维坐标系
\- 读取星座亮星坐标数据（可硬编码常见星座），绘制圆点与连线
\- 支持缩放（可选）

\---

\## 七、开发阶段规划（六次）

| 开发次数 | 阶段目标           | 具体任务                                                      |
| -------- | ------------------ | ------------------------------------------------------------- |
| 第1次    | 环境搭建与基础框架 | 数据库建表、实体类编写、DBUtil封装、LoginFrame与MainFrame骨架 |
| 第2次    | 用户管理完成       | 注册登录逻辑、个人信息维护、管理员用户管理                    |
| 第3次    | 星体与器材CRUD     | 星体管理面板、器材管理面板、完整增删改查                      |
| 第4次    | 观测记录管理       | 观测记录增删改查、关联器材选择、多条件查询                    |
| 第5次    | 统计可视化         | 集成JFreeChart、实现统计图表与星座分布图                      |
| 第6次    | 收尾与文档         | 报告导出、数据校验完善、界面美化、撰写项目文档                |

\---

\## 八、数据库建表 SQL 脚本模板

```sql

CREATE DATABASE IF NOT EXISTS astrolog DEFAULT CHARACTER SET utf8mb4;
USE astrolog;


CREATE TABLE users (
   user_id INT PRIMARY KEY AUTO_INCREMENT,
   username VARCHAR(50) UNIQUE NOT NULL,
   password VARCHAR(255) NOT NULL,
   role ENUM('observer','admin') NOT NULL DEFAULT 'observer',
   city VARCHAR(100),
   default_lat DECIMAL(9,6),
   default_lon DECIMAL(9,6),
   create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE celestial_bodies (
   body_id INT PRIMARY KEY AUTO_INCREMENT,
   name VARCHAR(100) NOT NULL,
   type VARCHAR(20) NOT NULL,
   constellation VARCHAR(50),
   ra_h INT,
   ra_m INT,
   dec_deg INT,
   dec_min INT,
   magnitude DECIMAL(4,2),
   best_season VARCHAR(20),
   description TEXT,
   image_path VARCHAR(255)
);



CREATE TABLE equipment (
   equip_id INT PRIMARY KEY AUTO_INCREMENT,
   user_id INT NOT NULL,
   name VARCHAR(100) NOT NULL,
   type VARCHAR(50),
   aperture DECIMAL(5,2),
   focal_length INT,
   description TEXT,
   FOREIGN KEY (user_id) REFERENCES users(user_id)

);



CREATE TABLE observations (
   obs_id INT PRIMARY KEY AUTO_INCREMENT,
   user_id INT NOT NULL,
   body_id INT NOT NULL,
   obs_time DATETIME NOT NULL,
   location_lat DECIMAL(9,6),
   location_lon DECIMAL(9,6),
   weather VARCHAR(50),
   seeing TINYINT,
   note TEXT,
   create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (user_id) REFERENCES users(user_id),
   FOREIGN KEY (body_id) REFERENCES celestial_bodies(body_id)

);



CREATE TABLE obs_equipment (
   obs_id INT NOT NULL,
   equip_id INT NOT NULL,
   PRIMARY KEY (obs_id, equip_id),
   FOREIGN KEY (obs_id) REFERENCES observations(obs_id),
   FOREIGN KEY (equip_id) REFERENCES equipment(equip_id)
);

```

\---

> 本文档为 AstroLog 系统的初步设计，涵盖需求规格、功能模块、数据库设计、系统架构与UI构思，可作为后续迭代开发的基准参考。

```

```
