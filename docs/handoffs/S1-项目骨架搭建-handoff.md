# S1 — 项目骨架搭建 交接文档

> **子阶段：** S1 项目骨架搭建
> **所属阶段：** 阶段一 — 基础奠基
> **编制日期：** 2026-05-14
> **上级会话状态：** 设计阶段全部完成，4 份规范文档已交付，准备进入编码阶段

---

## 1. 项目背景

AstroLog 是一个 Java SE 17 + Swing + MySQL 8.0 的天文观测日志与星体管理桌面应用。当前处于设计完成、编码启动的临界点。你的任务是在一个**全新的独立对话**中完成 S1 子阶段的全部开发工作。

**关键约束：**

- 体量目标：最终约 100 个 Java 文件、~14,100 行代码（参考项目租车系统 32 文件/4,200 行的 3 倍）
- 架构：三层架构 — `ui`（Swing）→ `service`（业务逻辑）→ `dao`（JDBC），严格分层，上层不跨层调用
- 数据库：MySQL 8.0 + JDBC + 自研连接池，不使用 ORM
- 所有 SQL 使用 PreparedStatement，杜绝 SQL 注入

**你的对话需要自行携带的上下文文件：**

- `CLAUDE.md` — 项目总览和架构约定
- `docs/02-架构设计文档-SAD.md` — 包结构、数据库表定义、设计模式、安全设计
- `docs/03-开发执行计划.md` — S1 任务清单（S1 部分）
- 参考项目 `租车系统/租车/` 源码 — DBUtil 连接池和 BaseDao 模板方法参考实现

---

## 2. S1 目标与范围

### 2.1 核心目标

建立可编译、可运行的空项目骨架。数据库全部 11 张表就绪。LoginFrame 和 MainFrame 可视化但**不实现业务逻辑**（登录校验留到 S2）。

### 2.2 范围边界

**S1 要做的事：**

| 类别     | 内容                                                                                                               |
| -------- | ------------------------------------------------------------------------------------------------------------------ |
| 构建系统 | Maven pom.xml，含全部依赖声明                                                                                      |
| 数据库   | 完整 11 张表建表脚本，可重复执行                                                                                   |
| 包结构   | com.astrolog 下全部 7 个子包创建                                                                                   |
| 实体层   | 13 个实体类 + 5 个枚举，完整字段与 getter/setter                                                                   |
| 配置层   | DBConfig、AppConfig、ThemeConfig                                                                                   |
| 基础设施 | DBUtil 连接池、BaseDao 模板方法、Validator 框架                                                                    |
| 工具类   | PasswordUtil、DateUtil、JsonDataLoader                                                                             |
| UI 骨架  | LoginFrame（星空背景+登录面板占位）、MainFrame（菜单栏+导航+CardLayout+状态栏）、DashboardPanel（欢迎页+快捷入口） |
| 入口     | AppMain（初始化 DBUtil → 显示 LoginFrame）                                                                         |
| 测试     | DBUtil 连接池单元测试、Validator 单元测试（共 ~5 例）                                                              |

**S1 不做的事：**

| 类别                                                | 留到哪个子阶段 |
| --------------------------------------------------- | -------------- |
| 登录/注册逻辑                                       | S2             |
| 任何 DAO 实现（UserDao 等）                         | S2+            |
| 任何 Service 层                                     | S2+            |
| 除 LoginFrame/MainFrame/DashboardPanel 外的功能面板 | S3+            |
| 图表、报告、导出                                    | S6/S9          |
| 主题切换的完整实现（仅定义 ThemeConfig 配色常量）   | S6+            |
| 国际化（仅预留目录结构）                            | S2+            |

---

## 3. 参考代码模式

### 3.1 DBUtil 连接池模式

参照 `租车系统/租车/src/com/rental/repository/DBConnectionManager.java`。

**核心设计：**

- 单例模式：`private static DBUtil instance` + `public static DBUtil getInstance()`
- 连接池容器：`private final BlockingQueue<Connection> pool`
- 默认参数：maxConnections=10, minConnections=2, initialConnections=5
- 构造流程：`loadConfig()` → `initPool()`，initPool 中预创建 initialConnections 个连接并 offer 入队
- 获取连接 `getConnection()`：`pool.poll()` → 若 null 且活跃数 < max → `DriverManager.getConnection()` 新创建 → 否则 `pool.take()` 阻塞等待
- 释放连接 `releaseConnection(conn)`：`conn.setAutoCommit(true)` 重置状态 → `pool.offer(conn)` 归还
- 关闭连接池 `shutdown()`：遍历 pool 逐个物理关闭

**db.properties 格式：**

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/astrolog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
jdbc.username=root
jdbc.password=root
pool.maxConnections=10
pool.minConnections=2
pool.initialConnections=5
```

文件位置：`src/main/resources/db.properties`（Maven 标准目录）。

### 3.2 BaseDao 模板方法模式

参照 `租车系统/租车/src/com/rental/repository/VehicleRepository.java` 和 `RentalRepository.java` 中重复的 JDBC 样板代码，提取为 `BaseDao<T>` 泛型基类。

**需要抽取的关键方法：**

```java
public abstract class BaseDao<T> {

    // 获取连接（委托给 DBUtil）
    protected Connection getConnection() throws SQLException;

    // 统一资源释放：rs → stmt → conn(归还池)
    protected void closeResources(ResultSet rs, Statement stmt, Connection conn);

    // 模板：执行查询，接受 RowMapper 函数式接口
    protected List<T> executeQuery(String sql, Object[] params, RowMapper<T> mapper);

    // 模板：执行更新（INSERT/UPDATE/DELETE），返回影响行数
    protected int executeUpdate(String sql, Object[] params);

    // 模板：执行插入并返回自增主键
    protected int executeInsert(String sql, Object[] params);

    // 检查表是否有数据
    protected boolean dataExists(String tableName);
}

// 函数式接口：将 ResultSet 当前行映射为实体对象
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}
```

**关键实现细节：**

- `executeQuery` 中：获取连接 → prepareStatement → 设置参数（循环 `stmt.setObject(i+1, params[i])`）→ executeQuery → while(rs.next) 调用 mapper.mapRow(rs) 收集到 List → finally closeResources
- `executeUpdate` 中：同上但执行 executeUpdate，返回 `stmt.getUpdateCount()`
- `executeInsert` 中：prepareStatement 时传入 `Statement.RETURN_GENERATED_KEYS`，执行后从 `stmt.getGeneratedKeys()` 获取自增 ID
- `closeResources` 中：每个关闭操作独立 try-catch，conn 使用 DBUtil.getInstance().releaseConnection() 归还

---

## 4. 详细任务清单

### 任务执行顺序（重要）

按以下顺序创建文件，确保每一步的依赖已就绪：

```
第 1 步  → pom.xml, sql/init.sql
第 2 步  → src/main/resources/db.properties
第 3 步  → 所有枚举类 (enums/)
第 4 步  → 所有实体类 (model/)
第 5 步  → config/ 包 (DBConfig, AppConfig, ThemeConfig)
第 6 步  → util/ 包 (DBUtil, PasswordUtil, DateUtil, JsonDataLoader, Validator, RowMapper)
第 7 步  → dao/BaseDao.java
第 8 步  → ui/frame/ (LoginFrame, MainFrame)
第 9 步  → ui/panel/DashboardPanel.java
第 10 步 → ui/component/ThemeManager.java
第 11 步 → AppMain.java
第 12 步 → 测试类
```

### 第 1 步：Maven 配置 + 建表脚本

**pom.xml** — 根目录 `D:\AstroLog-Design\pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.astrolog</groupId>
    <artifactId>astrolog</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>AstroLog</name>
    <description>天文观测日志与星体管理系统</description>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- MySQL JDBC 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.0.33</version>
        </dependency>

        <!-- BCrypt 密码加密 -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>

        <!-- Gson JSON 解析 -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>

        <!-- JFreeChart 图表 -->
        <dependency>
            <groupId>org.jfree</groupId>
            <artifactId>jfreechart</artifactId>
            <version>1.5.4</version>
        </dependency>

        <!-- JasperReports 报表 -->
        <dependency>
            <groupId>net.sf.jasperreports</groupId>
            <artifactId>jasperreports</artifactId>
            <version>6.20.6</version>
        </dependency>

        <!-- JUnit 5 单元测试 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>

        <!-- Mockito Mock 框架 -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.7.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>5.7.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.astrolog.AppMain</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

**sql/init.sql** — 根目录 `D:\AstroLog-Design\sql\init.sql`

所有表使用 `CREATE TABLE IF NOT EXISTS`，可重复执行。引擎 InnoDB，字符集 utf8mb4。

11 张表的完整字段定义见 SAD 文档第 4.2 节。创建顺序（考虑外键依赖）：

```
1. users
2. celestial_bodies
3. observation_sites
4. equipment
5. observations       (FK → users, celestial_bodies, observation_sites)
6. obs_equipment      (FK → observations, equipment)
7. equipment_maintenance (FK → equipment)
8. observation_tags
9. obs_tag_relation   (FK → observations, observation_tags)
10. user_favorites    (FK → users, celestial_bodies)
11. operation_logs    (FK → users)
```

数据库名使用 `astrolog`（先 `CREATE DATABASE IF NOT EXISTS astrolog`，再 `USE astrolog`）。

### 第 2 步：db.properties

路径：`src/main/resources/db.properties`

内容见上文 3.1 节，数据库名改为 `astrolog`。

### 第 3 步：枚举类

路径：`src/main/java/com/astrolog/model/enums/`

| 文件               | 枚举值                                                                                                                                                                                       |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `UserRole.java`    | OBSERVER("observer"), ADMIN("admin")                                                                                                                                                         |
| `BodyType.java`    | STAR("恒星"), PLANET("行星"), NEBULA("星云"), CLUSTER("星团"), GALAXY("星系")                                                                                                                |
| `EquipType.java`   | TELESCOPE("望远镜"), EYEPIECE("目镜"), CAMERA("相机"), OTHER("其他")                                                                                                                         |
| `EquipStatus.java` | ACTIVE("在用"), MAINTENANCE("维修"), RETIRED("退役")                                                                                                                                         |
| `MoonPhase.java`   | NEW_MOON("新月"), WAXING_CRESCENT("蛾眉月"), FIRST_QUARTER("上弦月"), WAXING_GIBBOUS("盈凸月"), FULL_MOON("满月"), WANING_GIBBOUS("亏凸月"), LAST_QUARTER("下弦月"), WANING_CRESCENT("残月") |

每个枚举：`private final String displayName` + 构造器 + `getDisplayName()` + 静态方法 `fromString(String)` 用于从数据库值反序列化。

### 第 4 步：实体类

路径：`src/main/java/com/astrolog/model/`

全部 13 个实体类。每个类包含：

- `private` 字段（类型与数据库列对应）
- 无参构造器 + 全参构造器
- 所有字段的 getter/setter
- `toString()` 方法

实体列表及关键字段：

| 实体类                 | 关键字段（除 ID 外）                                                                                                                                           |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `User`                 | username, password, role(UserRole), avatarPath, city, defaultLat, defaultLon, loginAttempts, lockedUntil(LocalDateTime), lastLogin(LocalDateTime), createTime  |
| `CelestialBody`        | name, type(BodyType), constellation, raH, raM, decDeg, decMin, magnitude(BigDecimal), distanceLy, messierNumber, ngcNumber, bestSeason, description, imagePath |
| `Observation`          | userId, bodyId, siteId(nullable), obsTime(LocalDateTime), locationLat, locationLon, weather, seeing, moonPhase(MoonPhase), note, createTime                    |
| `Equipment`            | userId, name, type(EquipType), aperture(BigDecimal), focalLength, purchaseDate(LocalDate), status(EquipStatus), description                                    |
| `ObservationSite`      | userId, name, latitude, longitude, altitude, bortleScale, bestTime                                                                                             |
| `EquipmentMaintenance` | equipId, maintDate(LocalDate), description, cost(BigDecimal), nextMaintDate(LocalDate)                                                                         |
| `ObservationTag`       | name, color（如 "#FF5733"）                                                                                                                                    |
| `UserFavorite`         | userId, bodyId, createTime                                                                                                                                     |
| `OperationLog`         | userId, operation, detail, ipAddress, createTime                                                                                                               |
| `MessierObject`        | messierNumber, name, type, constellation, magnitude, season, description（来自 JSON，非数据库表）                                                              |
| `ConstellationInfo`    | name, abbreviation, area, brightestStar, season, mythology（来自 JSON，非数据库表）                                                                            |
| `NightSkyData`         | visibleBodies(List), moonPhase, moonRise, moonSet, goldenWindow（计算结果的临时对象，非数据库表）                                                              |

**注意：** 处理日期时间的策略：

- 数据库 `DATETIME` / `TIMESTAMP` → Java `LocalDateTime`
- 数据库 `DATE` → Java `LocalDate`
- 金额/小数 → `BigDecimal`
- JDBC 驱动 8.0.33 支持 `LocalDateTime` 和 `LocalDate` 直接映射

### 第 5 步：配置类

路径：`src/main/java/com/astrolog/config/`

**DBConfig.java** — 数据库连接参数 POJO，从 db.properties 加载的配置值封装：

```java
public class DBConfig {
    private String driverClass;
    private String jdbcUrl;
    private String username;
    private String password;
    private int maxConnections;   // 默认 10
    private int minConnections;   // 默认 2
    private int initialConnections; // 默认 5
    // getter/setter
}
```

**AppConfig.java** — 应用级常量：

```java
public final class AppConfig {
    private AppConfig() {} // 禁止实例化

    public static final String APP_NAME = "AstroLog";
    public static final String APP_VERSION = "1.0-SNAPSHOT";
    public static final double DEFAULT_LAT = 39.9;   // 北京
    public static final double DEFAULT_LON = 116.4;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCK_DURATION_MINUTES = 30;
}
```

**ThemeConfig.java** — 三套主题配色常量：

```java
public final class ThemeConfig {
    private ThemeConfig() {}

    // 每套主题包含：背景色、前景色、面板色、按钮色、强调色、表格色、字体
    // 亮色主题 LIGHT
    // 暗色主题 DARK
    // 星空主题 STARRY

    public record Theme(Color bg, Color fg, Color panelBg, Color buttonBg,
                        Color accent, Color tableBg, Font font) {}

    public static final Theme LIGHT = new Theme(/*...*/);
    public static final Theme DARK = new Theme(/*...*/);
    public static final Theme STARRY = new Theme(/*...*/);
}
```

配色参考：

- **亮色：** bg=Color.WHITE, fg=Color.BLACK, panelBg=new Color(240,240,240), accent=new Color(0,102,204)
- **暗色：** bg=new Color(43,43,43), fg=new Color(220,220,220), panelBg=new Color(60,63,65), accent=new Color(75,110,175)
- **星空：** bg=new Color(10,10,40), fg=new Color(200,210,240), panelBg=new Color(20,20,60), accent=new Color(180,150,80)

### 第 6 步：工具类

路径：`src/main/java/com/astrolog/util/`

**DBUtil.java** — 数据库连接池（核心，~200 行）

单例模式。完整实现参照 3.1 节模式。

```java
public class DBUtil {
    private static volatile DBUtil instance;
    private final BlockingQueue<Connection> pool;
    private DBConfig config;
    // 私有构造器：loadConfig() → initPool()
    // public static getInstance() — 双重检查锁
    // public Connection getConnection() — poll → create → take
    // public void releaseConnection(Connection conn) — setAutoCommit(true) → offer
    // public void shutdown() — 清理所有连接
    // private Connection createConnection() — DriverManager.getConnection()
    // public int getActiveCount() — max - pool.size()
    // public int getIdleCount() — pool.size()
}
```

**PasswordUtil.java** — BCrypt 封装（~40 行）

```java
public final class PasswordUtil {
    private PasswordUtil() {}
    public static String hash(String plainPassword)  // BCrypt.hashpw()
    public static boolean verify(String plain, String hashed)  // BCrypt.checkpw()
}
```

**DateUtil.java** — 天文日期工具（~100 行）

```java
public final class DateUtil {
    private DateUtil() {}
    // 根据日期计算月相（简化算法：基于农历近似）
    public static MoonPhase calculateMoonPhase(LocalDate date)
    // 判断给定日期和经纬度的黄金观测窗口（日落后 1-3 小时）
    public static boolean isGoldenWindow(LocalDateTime time, double lat, double lon)
    // 格式化坐标显示 "39°54'N, 116°24'E"
    public static String formatLat(double lat)
    public static String formatLon(double lon)
    // 赤经赤纬格式化 "RA 5h35m, Dec -5°23'"
    public static String formatRADec(int raH, int raM, int decDeg, int decMin)
}
```

**JsonDataLoader.java** — JSON 资源加载（~80 行）

```java
public class JsonDataLoader {
    private static final Gson gson = new Gson();
    // 从 classpath 加载 JSON 文件并解析为指定类型
    public static <T> List<T> loadList(String resourcePath, Class<T[]> clazz)
    public static <T> T loadObject(String resourcePath, Class<T> clazz)
}
```

**Validator.java** — 链式校验框架（~200 行）

装饰器模式实现。每个校验规则是一个 `ValidationRule` 函数式接口：

```java
@FunctionalInterface
public interface ValidationRule {
    ValidationResult validate(Object value);
}

public class ValidationResult {
    private boolean valid;
    private String message;
    // 静态工厂：success(), fail(String msg)
}

public class Validator {
    private final List<ValidationRule> rules = new ArrayList<>();

    public Validator addRule(ValidationRule rule) { rules.add(rule); return this; }
    public List<ValidationResult> validate(Object value) { /* 依次执行全部规则 */ }
    public ValidationResult validateFirst(Object value) { /* 返回第一个失败结果 */ }

    // 预置规则（静态工厂方法）：
    public static ValidationRule notNull(String msg)
    public static ValidationRule notBlank(String msg)
    public static ValidationRule minLength(int min, String msg)
    public static ValidationRule maxLength(int max, String msg)
    public static ValidationRule pattern(String regex, String msg)
    public static ValidationRule range(double min, double max, String msg)
    // 用户名规则：4-20 字符，字母/数字/下划线
    public static Validator usernameValidator()
    // 密码强度规则：最少 8 字符，含大小写字母+数字
    public static Validator passwordValidator()
    // 经纬度范围规则
    public static Validator latitudeValidator()
    public static Validator longitudeValidator()
}
```

### 第 7 步：BaseDao

路径：`src/main/java/com/astrolog/dao/BaseDao.java`

泛型抽象类，参照 3.2 节设计。约 150 行。

关键实现注意：

- `executeQuery` 中参数设置使用循环 `for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i])`
- `executeInsert` 使用 `PreparedStatement.RETURN_GENERATED_KEYS`
- `closeResources` 中三个关闭各用独立 try-catch
- RowMapper 作为 `protected` 内部函数式接口

### 第 8 步：UI 骨架

路径：`src/main/java/com/astrolog/ui/frame/`

**LoginFrame.java** — 登录界面骨架（~250 行）

```
┌──────────────────────────────────────┐
│                                      │
│          🌟 AstroLog 🌟             │
│     天文观测日志与星体管理系统       │
│                                      │
│     ┌──────────────────────┐        │
│     │  用户名: [________]  │        │
│     │  密码:   [________]  │        │
│     │  [登录]  [注册]      │        │
│     └──────────────────────┘        │
│                                      │
└──────────────────────────────────────┘
```

实现要点：

- extends JFrame，深色背景模拟星空（使用 Graphics 绘制随机白点作为星星）
- 居中登录面板（JPanel，半透明背景）
- 用户名 JTextField + 密码 JPasswordField + 登录 JButton + 注册 JButton
- 按钮点击事件留桩（`System.out.println("登录功能将在S2实现")`）
- 窗口大小 800x600，居中显示，不可调整大小 `setResizable(false)`

**MainFrame.java** — 主界面骨架（~300 行）

```
┌─────────────────────────────────────────────────┐
│ 菜单栏                                            │
├──────────┬──────────────────────────────────────┤
│          │                                      │
│ 导航按钮  │     CardLayout 内容区                 │
│          │                                      │
│ ─────── │     DashboardPanel（默认显示）        │
│ 仪表盘   │                                      │
│ 星体库   │                                      │
│ 我的观测  │                                      │
│ 器材柜   │                                      │
│ 观测地   │                                      │
│ 今夜星空  │                                      │
│ 统计图表  │                                      │
│ 梅西耶   │                                      │
│ 星座文化  │                                      │
│ 系统设置  │                                      │
│          │                                      │
├──────────┴──────────────────────────────────────┤
│ 状态栏: 当前用户: xxx | 角色: xxx               │
└─────────────────────────────────────────────────┘
```

实现要点：

- extends JFrame，最大化打开，使用 BorderLayout
- 菜单栏（JMenuBar）：文件（退出）、数据管理（占位）、统计分析（占位）、帮助（关于）
- 左侧导航区（JPanel, GridLayout 垂直排列 10 个 JButton）：仪表盘、星体库、我的观测、器材柜、观测地、今夜星空、统计图表、梅西耶马拉松、星座文化馆、系统设置
- 右侧内容区（JPanel with CardLayout）：10 张卡片，S1 只创建 DashboardPanel 为第一张，其余 9 张为空白 JPanel（占位，带标签文字如"星体库 - 将在S3实现"）
- 底部状态栏（JLabel）：显示占位文字
- 当前用户和角色信息用占位值（S2 完善）
- 窗口关闭时调用 `DBUtil.getInstance().shutdown()`

### 第 9 步：DashboardPanel

路径：`src/main/java/com/astrolog/ui/panel/DashboardPanel.java`

欢迎仪表盘（~200 行）：

- 顶部：欢迎标题 + 当前日期时间
- 中部：4-6 个快捷入口卡片（使用 JPanel 排列），点击可跳转到对应功能面板（通过回调接口通知 MainFrame 切换 CardLayout 卡片）：
  - 星体库、添加观测、今夜星空、统计图表、梅西耶马拉松、系统设置
- 底部：快速摘要区（S1 阶段显示占位统计数字 "0 次观测 / 0 个星体 / 0 件器材"）

### 第 10 步：ThemeManager

路径：`src/main/java/com/astrolog/ui/component/ThemeManager.java`

单例 + 观察者模式（~150 行）：

```java
public class ThemeManager {
    private static ThemeManager instance;
    private ThemeConfig.Theme currentTheme;
    private final List<ThemeObserver> observers = new ArrayList<>();

    private ThemeManager() { currentTheme = ThemeConfig.DARK; } // 默认暗色

    public static ThemeManager getInstance()
    public void setTheme(ThemeConfig.Theme theme)  // 切换主题并通知所有观察者
    public ThemeConfig.Theme getCurrentTheme()
    public void register(ThemeObserver observer)
    public void unregister(ThemeObserver observer)
}

// 观察者接口：所有需要跟随主题变换的面板实现此接口
public interface ThemeObserver {
    void onThemeChanged(ThemeConfig.Theme newTheme);
}
```

### 第 11 步：AppMain

路径：`src/main/java/com/astrolog/AppMain.java`

```java
public class AppMain {
    public static void main(String[] args) {
        // 1. 初始化数据库连接池
        DBUtil.getInstance();
        // 2. 设置 Swing LookAndFeel（FlatLaf 或系统原生）
        // 3. 在 EDT 中启动 LoginFrame
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
```

约 50 行。S1 阶段 LoginFrame 不包含实际登录逻辑。

### 第 12 步：测试

路径：`src/test/java/com/astrolog/`

**DBUtilTest.java**（`src/test/java/com/astrolog/util/DBUtilTest.java`）：

```java
class DBUtilTest {
    @Test void testGetInstance()           // 单例不为 null
    @Test void testGetConnection()         // 获取连接成功且未关闭
    @Test void testReleaseConnection()     // 释放后池大小恢复
}
```

**ValidatorTest.java**（`src/test/java/com/astrolog/util/ValidatorTest.java`）：

```java
class ValidatorTest {
    @Test void testNotNull()              // null 值校验失败
    @Test void testMinLength()            // 过短值校验失败
    @Test void testUsernameValidator()    // 合法用户名通过
    @Test void testPasswordValidator()    // 弱密码拒绝
}
```

注意：DBUtil 测试需要 MySQL 运行且 astrolog 数据库已创建。

---

## 5. Maven 项目目录结构

S1 完成后的完整目录结构：

```
D:\AstroLog-Design\
├── pom.xml
├── CLAUDE.md (已有)
├── AstroLog-Design.md (已有)
├── sql/
│   └── init.sql
├── docs/
│   ├── 01-软件需求规格说明书-SRS.md (已有)
│   ├── 02-架构设计文档-SAD.md (已有)
│   ├── 03-开发执行计划.md (已有)
│   ├── 04-测试与审核流程.md (已有)
│   └── handoffs/
│       └── S1-项目骨架搭建-handoff.md (本文件)
└── src/
    ├── main/
    │   ├── java/com/astrolog/
    │   │   ├── AppMain.java
    │   │   ├── config/
    │   │   │   ├── AppConfig.java
    │   │   │   ├── DBConfig.java
    │   │   │   └── ThemeConfig.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── CelestialBody.java
    │   │   │   ├── Observation.java
    │   │   │   ├── Equipment.java
    │   │   │   ├── ObservationSite.java
    │   │   │   ├── EquipmentMaintenance.java
    │   │   │   ├── ObservationTag.java
    │   │   │   ├── UserFavorite.java
    │   │   │   ├── OperationLog.java
    │   │   │   ├── MessierObject.java
    │   │   │   ├── ConstellationInfo.java
    │   │   │   ├── NightSkyData.java
    │   │   │   └── enums/
    │   │   │       ├── UserRole.java
    │   │   │       ├── BodyType.java
    │   │   │       ├── EquipType.java
    │   │   │       ├── EquipStatus.java
    │   │   │       └── MoonPhase.java
    │   │   ├── dao/
    │   │   │   ├── BaseDao.java
    │   │   │   └── RowMapper.java (BaseDao 内部接口)
    │   │   ├── service/
    │   │   │   └── (S1 为空，留到 S2+)
    │   │   ├── ui/
    │   │   │   ├── frame/
    │   │   │   │   ├── LoginFrame.java
    │   │   │   │   └── MainFrame.java
    │   │   │   ├── panel/
    │   │   │   │   └── DashboardPanel.java
    │   │   │   ├── dialog/
    │   │   │   │   └── (S1 为空)
    │   │   │   └── component/
    │   │   │       └── ThemeManager.java
    │   │   └── util/
    │   │       ├── DBUtil.java
    │   │       ├── PasswordUtil.java
    │   │       ├── DateUtil.java
    │   │       ├── JsonDataLoader.java
    │   │       └── Validator.java
    │   └── resources/
    │       ├── db.properties
    │       └── (i18n 预留，S2 实现)
    └── test/
        └── java/com/astrolog/
            └── util/
                ├── DBUtilTest.java
                └── ValidatorTest.java
```

---

## 6. 验收标准

S1 完成的必要条件（逐项检查）：

| #   | 验收项                                                                                                       | 验证方法                                |
| --- | ------------------------------------------------------------------------------------------------------------ | --------------------------------------- |
| 1   | `mvn clean compile` 零错误                                                                                   | 执行 Maven 编译                         |
| 2   | `mvn test` 全部通过（~5 个测试）                                                                             | 执行测试                                |
| 3   | AppMain 启动后 LoginFrame 可见，星空背景正常绘制                                                             | 手动运行 `mvn exec:java` 或 `java -jar` |
| 4   | LoginFrame 点击登录按钮有占位响应                                                                            | 手动点击                                |
| 5   | MainFrame 可见（需临时在 LoginFrame 登录按钮中直接 new MainFrame 来测试），菜单栏/导航/CardLayout/状态栏正常 | 手动测试                                |
| 6   | 执行 `sql/init.sql` 后 11 张表全部创建成功                                                                   | `SHOW TABLES` 验证                      |
| 7   | DBUtil 连接池可正常获取和释放连接                                                                            | DBUtilTest 通过                         |
| 8   | 项目目录结构符合第 5 节规划                                                                                  | 逐项核对                                |
| 9   | 所有实体类字段与数据库表列一一对应                                                                           | 对照 SAD 检查                           |

---

## 7. 常见注意事项

1. **Maven 目录结构：** `src/main/java` 和 `src/test/java` 必须严格遵循 Maven 约定，否则编译失败
2. **JDBC 驱动类名：** MySQL 8.0 使用 `com.mysql.cj.jdbc.Driver`（带 cj），不是旧版的 `com.mysql.jdbc.Driver`
3. **数据库时区：** 连接 URL 必须带 `serverTimezone=Asia/Shanghai`，否则可能出现时区错误
4. **密码加密：** BCrypt 的 `gensalt()` 默认强度 10，在桌面应用中足够；不要自己实现哈希
5. **Swing 线程安全：** 所有 Swing 组件操作必须在 Event Dispatch Thread (EDT) 中执行，使用 `SwingUtilities.invokeLater()`
6. **连接池线程安全：** ArrayBlockingQueue 本身线程安全，但 `getActiveCount()` 计算 (`max - pool.size()`) 不是原子的，仅用于日志/监控
7. **资源释放：** try-catch-finally 中每个资源的关闭必须独立 try-catch，防止一个关闭失败影响其他资源释放
8. **不做过度设计：** S1 只做骨架，不要实现任何业务逻辑。Service 层目录留空即可。MainFrame 中除 DashboardPanel 外的 9 个面板用空白 JPanel 占位
