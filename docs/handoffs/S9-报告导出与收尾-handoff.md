# S9 — 报告导出与收尾 交接文档

> **子阶段：** S9 报告导出 + 系统配置 + 收尾（最终子阶段）
> **所属阶段：** 阶段三 — 扩展功能与收尾
> **编制日期：** 2026-05-27
> **上级会话状态：** S8 已完成（127/127 测试通过，G1-G4 审核通过），项目已接近尾声

---

## 1. 前置状态速览

### 1.1 已交付资产

| 资产                 | 路径                                 | S9 用途                                                                             |
| -------------------- | ------------------------------------ | ----------------------------------------------------------------------------------- |
| `StatsService`       | S6 `service/StatsService.java`       | 报告数据源：totalObservations/countByYear/countByMonth/countByBodyType/topEquipment |
| `MessierService`     | S8 `service/MessierService.java`     | 梅西耶证书数据：getFullCatalog/getProgress/isCertEligible                           |
| `ThemeManager`       | S1 `ui/component/ThemeManager.java`  | 单例 + 观察者模式，SettingsPanel 调用 setTheme()                                    |
| `ThemeConfig`        | S1 `config/ThemeConfig.java`         | LIGHT/DARK/STARRY 三套配色                                                          |
| `ThemeObserver`      | S1 `ui/component/ThemeObserver.java` | 接口，所有面板实现 onThemeChanged()                                                 |
| `DBUtil`             | S1 `util/DBUtil.java`                | BackupService 读取表结构                                                            |
| `JasperReports 6.20` | pom.xml 依赖                         | PDF 报告引擎                                                                        |
| `i18n` 资源          | S2 `resource/i18n/`                  | messages_zh/en.properties，LoginFrame 接入                                          |
| `MainFrame`          | `ui/frame/MainFrame.java`            | `getCurrentUserObject()` / `switchTo()`                                             |
| `DashboardPanel`     | `ui/panel/DashboardPanel.java`       | 摘要区数字目前为静态占位 "0 次观测 / 0 个星体 / 0 件器材"                           |

### 1.2 MainFrame 当前状态

NAV_LABELS[9] = "系统设置"，当前为占位 JPanel，需替换为 SettingsPanel。

### 1.3 S2-S8 全部偏差经验（S9 必须遵循）

| 经验                           | S9 应用                                                                |
| ------------------------------ | ---------------------------------------------------------------------- |
| 死代码清除、通配符 import 禁止 | ExportService/BackupService/SettingsPanel                              |
| Service 测试构造器             | ExportService(StatsService, MessierService); BackupService(DataSource) |
| Dialog 末尾 setVisible(true)   | ReportViewDialog / BackupRestoreDialog                                 |
| Unicode 符号 Font.SANS_SERIF   | SettingsPanel 主题选择图标                                             |
| 行数估算下调 ~25%              | 实际约 ~1,500 行 vs 交接估算 ~2,000 行                                 |
| JasperReports 中文字体方框     | 参考 S6 ChartUtil.applyChineseFont() 模式                              |
| JSON 数据量合理                | 不生成额外大数据文件                                                   |

---

## 2. S9 目标与范围

### 2.1 核心目标

实现 F6 报告导出（HTML + PDF）+ F10 系统配置（主题切换 + 备份恢复）+ i18n 完整接入 + Dashboard 动态化 + 项目打包交付。

### 2.2 新增文件清单（9 个）

| #   | 文件                       | 包                       | 行数       |
| --- | -------------------------- | ------------------------ | ---------- |
| 1   | `ExportService.java`       | `com.astrolog.service`   | ~250       |
| 2   | `ExportUtil.java`          | `com.astrolog.util`      | ~150       |
| 3   | `BackupService.java`       | `com.astrolog.service`   | ~200       |
| 4   | `ReportViewDialog.java`    | `com.astrolog.ui.dialog` | ~120       |
| 5   | `BackupRestoreDialog.java` | `com.astrolog.ui.dialog` | ~160       |
| 6   | `SettingsPanel.java`       | `com.astrolog.ui.panel`  | ~180       |
| 7   | `annual_report.jrxml`      | `resource/report/`       | ~80 行模板 |
| 8   | `messier_cert.jrxml`       | `resource/report/`       | ~60 行模板 |
| 9   | `ExportServiceTest.java`   | test `unit/service`      | ~120       |

### 2.3 修改文件（5 个）

| #   | 文件                  | 改动                                                                                                        |
| --- | --------------------- | ----------------------------------------------------------------------------------------------------------- |
| 1   | `MainFrame.java`      | "系统设置"占位 → SettingsPanel；About 版本号更新为 1.0                                                      |
| 2   | `DashboardPanel.java` | 摘要三组数字从静态 → 调用 StatsService 动态查询                                                             |
| 3   | `LoginFrame.java`     | 所有按钮/标签文字从硬编码 → ResourceBundle 读取                                                             |
| 4   | `StarMapCanvas.java`  | S8 遗留：硬编码 15 星座 → 从 constellations.json 加载全部 88 星座连线数据 + 从 BodyDao 读取星体位置绘制标记 |
| 5   | `MessierPanel.java`   | S8 遗留：双击天体行 → `mainFrame.switchTo("星体库")` 并在 CelestialBodyPanel 中高亮对应星体                 |

### 2.4 S8 遗留项处理（S9 收尾补充）

| 遗留项                                  | S8 状态  | S9 处理                                                                                                   |
| --------------------------------------- | -------- | --------------------------------------------------------------------------------------------------------- |
| StarMapCanvas 仅硬编码 15 星座连线/标签 | 未覆盖   | `constellations.json` 增补每星座 `lines` 字段（亮星间连线坐标段数组）；StarMapCanvas 改为从 JSON 动态加载 |
| StarMapCanvas 未显示全部星体位置        | 未覆盖   | 新增 `drawAllBodies()`：从 `BodyDao.findAll()` 读取所有星体 RA/Dec，在星图中绘制灰色小圆点                |
| 梅西耶详情跳转星体库                    | 部分覆盖 | MessierPanel 双击行 → `mainFrame.switchTo("星体库")`；CelestialBodyPanel 新增 `highlightBody(bodyId)`     |

### 2.5 S9 交付物（项目最终产出）

| 类别 | 文件                                              |
| ---- | ------------------------------------------------- |
| 报告 | HTML 年度观测报告 + PDF 年度报告 + 梅西耶证书 PDF |
| 系统 | 主题切换（3 套）、字体大小调节、数据备份/恢复     |
| i18n | LoginFrame/MainFrame/SettingsPanel 中英双语       |
| 文档 | 用户操作手册、安装部署指南                        |
| 交付 | `astrolog.jar` fat jar（含所有依赖）              |

---

## 3. 详细实现规格

### 3.1 ExportService — 报告导出服务

**文件：** `src/main/java/com/astrolog/service/ExportService.java`

```java
package com.astrolog.service;

import com.astrolog.model.User;
import com.astrolog.util.ExportUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExportService {

    private final StatsService statsService;
    private final MessierService messierService;

    public ExportService() {
        this.statsService = new StatsService();
        this.messierService = new MessierService();
    }

    // package-private 测试构造器
    ExportService(StatsService statsService, MessierService messierService) {
        this.statsService = statsService;
        this.messierService = messierService;
    }

    // ==================== HTML 年度报告 ====================

    /**
     * @return 生成的 .html 文件路径
     */
    public String generateHtmlReport(User user, int year) throws IOException {
        int userId = user.getUserId();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
           .append("<title>AstroLog 年度观测报告 ").append(year).append("</title>")
           .append("<style>body{font-family:'Microsoft YaHei',sans-serif;")
           .append("max-width:800px;margin:40px auto;color:#333;}")
           .append("h1{color:#1a5276;}")
           .append("table{border-collapse:collapse;width:100%;margin:16px 0;}")
           .append("th,td{border:1px solid #ccc;padding:8px 12px;text-align:left;}")
           .append("th{background:#f0f4f8;}.stat{font-size:24px;font-weight:bold;color:#2e86c1;}")
           .append("</style></head><body>");

        // 标题 + 用户信息
        html.append("<h1>AstroLog 年度观测报告</h1>")
           .append("<p>观测者: ").append(user.getUsername())
           .append(" | 年度: ").append(year)
           .append(" | 生成时间: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
           .append("</p><hr>");

        // 观测摘要
        long total = statsService.totalObservations(userId);
        long distinctBodies = statsService.distinctBodiesObserved(userId);
        long totalEquip = statsService.totalEquipmentUsed(userId);

        html.append("<h2>年度摘要</h2>")
           .append("<p><span class='stat'>").append(total).append("</span> 次观测</p>")
           .append("<p><span class='stat'>").append(distinctBodies).append("</span> 个星体</p>")
           .append("<p><span class='stat'>").append(totalEquip).append("</span> 件器材</p>");

        // 星体类型分布
        Map<String, Long> typeDist = new LinkedHashMap<>();
        statsService.countByBodyType(userId).forEach((k, v) ->
            typeDist.put(k.getDisplayName(), v));
        html.append("<h2>星体类型分布</h2><table><tr><th>类型</th><th>次数</th></tr>");
        for (var e : typeDist.entrySet()) {
            html.append("<tr><td>").append(e.getKey())
                .append("</td><td>").append(e.getValue()).append("</td></tr>");
        }
        html.append("</table>");

        // 器材使用排行 Top 10
        var topEquip = statsService.topEquipment(userId);
        html.append("<h2>器材使用排行</h2><table><tr><th>器材</th><th>使用次数</th></tr>");
        for (var e : topEquip) {
            html.append("<tr><td>").append(e.getKey())
                .append("</td><td>").append(e.getValue()).append("</td></tr>");
        }
        html.append("</table>");

        html.append("<hr><p style='color:#999;font-size:12px;'>")
           .append("由 AstroLog v1.0 自动生成</p></body></html>");

        // 写入文件
        Path outputDir = Paths.get("reports");
        Files.createDirectories(outputDir);
        Path file = outputDir.resolve("annual_report_" + year + ".html");
        Files.writeString(file, html.toString(), StandardCharsets.UTF_8);
        return file.toAbsolutePath().toString();
    }

    // ==================== PDF 年度报告 ====================

    /**
     * 使用 JasperReports 生成 PDF 报告
     * @return 生成的 .pdf 文件路径
     */
    public String generatePdfReport(User user, int year) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("USERNAME", user.getUsername());
        params.put("YEAR", String.valueOf(year));
        params.put("TOTAL_OBS", statsService.totalObservations(user.getUserId()));
        params.put("DISTINCT_BODIES", statsService.distinctBodiesObserved(user.getUserId()));
        params.put("TOTAL_EQUIP", statsService.totalEquipmentUsed(user.getUserId()));
        params.put("GENERATED_AT", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        Path outputDir = Paths.get("reports");
        Files.createDirectories(outputDir);
        String outputPath = outputDir.resolve("annual_report_" + year + ".pdf")
            .toAbsolutePath().toString();

        ExportUtil.exportPdf("report/annual_report.jrxml", params, null, outputPath);
        return outputPath;
    }

    // ==================== 梅西耶证书 ====================

    public String generateMessierCert(User user) throws Exception {
        int userId = user.getUserId();
        Set<Integer> observed = messierService.getObservedNumbers(userId);

        if (!messierService.isCertEligible(observed)) {
            throw new IllegalStateException("未完成全部 110 个梅西耶天体观测");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("USERNAME", user.getUsername());
        params.put("COMPLETED_COUNT", observed.size());
        params.put("TOTAL_COUNT", 110);
        params.put("COMPLETED_AT", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        Path outputDir = Paths.get("reports");
        Files.createDirectories(outputDir);
        String outputPath = outputDir.resolve("messier_cert.pdf")
            .toAbsolutePath().toString();

        ExportUtil.exportPdf("report/messier_cert.jrxml", params, null, outputPath);
        return outputPath;
    }
}
```

### 3.2 ExportUtil — JasperReports 封装

**文件：** `src/main/java/com/astrolog/util/ExportUtil.java`

```java
package com.astrolog.util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ExportUtil {

    private ExportUtil() {}

    /**
     * 编译 .jrxml 模板并导出 PDF
     * @param templatePath 模板 classpath 路径
     * @param params       报表参数
     * @param dataSource   数据源（可为 null，参数驱动报表）
     * @param outputPath   输出 .pdf 路径
     */
    public static void exportPdf(String templatePath, Map<String, Object> params,
                                  JRBeanCollectionDataSource dataSource,
                                  String outputPath) throws Exception {
        InputStream is = ExportUtil.class.getClassLoader()
            .getResourceAsStream(templatePath);
        if (is == null) throw new FileNotFoundException("模板未找到: " + templatePath);

        JasperReport report = JasperCompileManager.compileReport(is);
        JasperPrint print = dataSource != null
            ? JasperFillManager.fillReport(report, params, dataSource)
            : JasperFillManager.fillReport(report, params, new JREmptyDataSource());

        JasperExportManager.exportReportToPdfFile(print, outputPath);
    }
}
```

### 3.3 JasperReports 模板

**文件：** `src/main/resources/report/annual_report.jrxml`

简易模板（~80 行 XML），关键结构：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              name="AnnualReport" pageWidth="595" pageHeight="842"
              leftMargin="40" rightMargin="40" topMargin="40" bottomMargin="40">
  <parameter name="USERNAME" class="java.lang.String"/>
  <parameter name="YEAR" class="java.lang.String"/>
  <parameter name="TOTAL_OBS" class="java.lang.Long"/>
  <parameter name="DISTINCT_BODIES" class="java.lang.Long"/>
  <parameter name="TOTAL_EQUIP" class="java.lang.Long"/>
  <parameter name="GENERATED_AT" class="java.lang.String"/>

  <title>
    <band height="100">
      <staticText><text>AstroLog 年度观测报告</text></staticText>
      <textField><textFieldExpression>$P{USERNAME}</textFieldExpression></textField>
    </band>
  </title>

  <detail>
    <band height="300">
      <!-- 观测次数/星体数/器材数 三个大数字 -->
      <textField><textFieldExpression>$P{TOTAL_OBS} + " 次观测"</textFieldExpression></textField>
      <!-- 生成时间 -->
      <textField><textFieldExpression>"生成时间: " + $P{GENERATED_AT}</textFieldExpression></textField>
    </band>
  </detail>
</jasperReport>
```

**文件：** `src/main/resources/report/messier_cert.jrxml`

类似结构（~60 行），包含用户名 + 完成数量 + 完成日期 + 装饰边框。

**关键点：**

- JasperReports 默认不支持中文显示，需在 pom.xml 中添加 `itext-asian` 依赖或使用支持中文的字体扩展
- S6 的 ChartUtil.applyChineseFont() 经验：在模板中设置 `pdfFontName="Microsoft YaHei"` 或使用 `fontExtensions`
- 简化方案：模板中嵌入字体（`<font fontName="STSong-Light" pdfFontName="STSong-Light"/>`），使用 JasperReports 内置亚洲字体

### 3.4 BackupService — 数据备份恢复

**文件：** `src/main/java/com/astrolog/service/BackupService.java`

```java
package com.astrolog.service;

import com.astrolog.util.DBUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    // 备份顺序（考虑外键依赖）
    private static final String[] TABLES = {
        "users", "celestial_bodies", "observation_sites",
        "equipment", "observations", "obs_equipment",
        "equipment_maintenance", "observation_tags",
        "obs_tag_relation", "user_favorites", "operation_logs"
    };

    public BackupService() {}

    /**
     * 导出全部数据为 SQL 文件
     * @return 备份文件路径
     */
    public String backup(String outputDir) throws Exception {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path file = Paths.get(outputDir, "astrolog_backup_" + timestamp + ".sql");
        Connection conn = DBUtil.getInstance().getConnection();

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("-- AstroLog 数据备份");
            writer.write(" (" + timestamp + ")\n\n");
            writer.write("USE astrolog;\n\n");

            for (String table : TABLES) {
                writer.write("-- 表: " + table + "\n");

                // 查询表的所有数据
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                while (rs.next()) {
                    StringBuilder sb = new StringBuilder("INSERT INTO ")
                        .append(table).append(" VALUES (");
                    for (int i = 1; i <= colCount; i++) {
                        if (i > 1) sb.append(", ");
                        String val = rs.getString(i);
                        if (val == null) {
                            sb.append("NULL");
                        } else if (meta.getColumnTypeName(i).contains("INT")
                                || meta.getColumnTypeName(i).contains("DECIMAL")) {
                            sb.append(val);  // 数字不加引号
                        } else {
                            sb.append("'").append(val.replace("'", "\\'")).append("'");
                        }
                    }
                    sb.append(");\n");
                    writer.write(sb.toString());
                }
                rs.close();
                stmt.close();
                writer.write("\n");
            }
        } finally {
            DBUtil.getInstance().releaseConnection(conn);
        }
        return file.toAbsolutePath().toString();
    }

    /**
     * 从 SQL 备份文件恢复数据
     * @param backupPath 备份文件路径
     */
    public ServiceResult restore(String backupPath) throws Exception {
        String content = Files.readString(Paths.get(backupPath), StandardCharsets.UTF_8);
        String[] statements = content.split(";\\s*\n");

        Connection conn = DBUtil.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);  // 事务保护
            Statement stmt = conn.createStatement();
            for (String sql : statements) {
                sql = sql.trim();
                if (sql.isEmpty() || sql.startsWith("--")) continue;
                stmt.execute(sql);
            }
            conn.commit();
            return ServiceResult.success("数据恢复成功");
        } catch (SQLException e) {
            conn.rollback();
            return ServiceResult.fail("恢复失败: " + e.getMessage());
        } finally {
            DBUtil.getInstance().releaseConnection(conn);
        }
    }
}
```

### 3.5 ReportViewDialog — 报告预览对话框

**文件：** `src/main/java/com/astrolog/ui/dialog/ReportViewDialog.java`

```
┌────────────────────────────────────┐
│ AstroLog 年度观测报告 [2026]       │
├────────────────────────────────────┤
│                                    │
│ JEditorPane (HTML 只读) 或         │
│ JScrollPane + JTextArea (文本模式) │
│                                    │
├────────────────────────────────────┤
│ [导出 PDF]  [导出 HTML]  [关闭]   │
└────────────────────────────────────┘
```

**实现要点：**

1. 构造器：`public ReportViewDialog(JFrame parent, String htmlContent, User user, int year)`
2. JEditorPane 设置 `setContentType("text/html")` + `setEditable(false)`
3. "导出 PDF" → `exportService.generatePdfReport(user, year)` → JOptionPane 显示文件路径
4. "导出 HTML" → `Files.writeString()` → 同上
5. **【强制】构造器末尾 `setVisible(true)`**

### 3.6 BackupRestoreDialog — 备份恢复对话框

**文件：** `src/main/java/com/astrolog/ui/dialog/BackupRestoreDialog.java`

```
┌────────────────────────────────────┐
│ 数据备份与恢复                      │
├────────────────────────────────────┤
│                                    │
│ ┌─ 备份 ─────────────────────────┐ │
│ │ 将全部数据导出为 SQL 文件       │ │
│ │ 保存到: [____________] [浏览]  │ │
│ │ [开始备份]                     │ │
│ └────────────────────────────────┘ │
│                                    │
│ ┌─ 恢复 ─────────────────────────┐ │
│ │ 从 SQL 备份文件恢复数据         │ │
│ │ ⚠ 将覆盖当前数据库数据！       │ │
│ │ 选择文件: [____________] [浏览]│ │
│ │ [开始恢复]                     │ │
│ └────────────────────────────────┘ │
│                                    │
│ ┌─ 状态 ─────────────────────────┐ │
│ │ JTextArea (只读，显示日志)     │ │
│ └────────────────────────────────┘ │
│                                    │
│ [关闭]                              │
└────────────────────────────────────┘
```

**实现要点：**

1. 备份恢复操作在 `SwingWorker` 中执行（避免 EDT 阻塞）
2. 恢复前弹出严重警告确认对话框（"此操作将覆盖当前数据库全部数据，确定继续？"）
3. JFileChooser 设置文件过滤（.sql 文件）
4. **【强制】构造器末尾 `setVisible(true)`**

### 3.7 SettingsPanel — 系统设置面板

**文件：** `src/main/java/com/astrolog/ui/panel/SettingsPanel.java`

**布局结构（~180 行）：**

```
┌─────────────────────────────────────────────┐
│                                             │
│ ┌─ 外观设置 ──────────────────────────────┐ │
│ │ 主题: ○ 暗色主题  ○ 亮色主题  ○ 星空   │ │
│ │ 字体: ○ 小  ○ 中(默认)  ○ 大           │ │
│ └─────────────────────────────────────────┘ │
│                                             │
│ ┌─ 数据管理 ──────────────────────────────┐ │
│ │ [备份数据]  [恢复数据]                  │ │
│ └─────────────────────────────────────────┘ │
│                                             │
│ ┌─ 关于 ──────────────────────────────────┐ │
│ │ AstroLog v1.0                           │ │
│ │ 天文观测日志与星体管理系统              │ │
│ │ Java SE 17 + Swing + MySQL 8.0         │ │
│ │ 2026 © AstroLog Team                   │ │
│ └─────────────────────────────────────────┘ │
│                                             │
└─────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public SettingsPanel(User currentUser)`
2. 主题切换：JRadioButton 组 → 选中时调用 `ThemeManager.getInstance().setTheme(ThemeConfig.LIGHT/DARK/STARRY)` → 全局 UI 自动刷新（观察者模式）
3. 字体大小：JRadioButton 组 → 通过 UIManager 调整默认字体大小 → 需要遍历所有打开的 JFrame 并 `SwingUtilities.updateComponentTreeUI()`
4. "备份数据" → 打开 `BackupRestoreDialog`
5. "恢复数据" → 同上
6. 关于区域：静态 JLabel，展示版本信息

### 3.8 DashboardPanel — 摘要区动态化

**文件：** `src/main/java/com/astrolog/ui/panel/DashboardPanel.java`

**修改：** 三组静态数字从 `"0 次观测 / 0 个星体 / 0 件器材"` 改为调用 `StatsService` 动态查询：

```java
// 在构造器中或 refresh() 方法中：
StatsService statsService = new StatsService();
int userId = currentUser.getUserId();

long totalObs = statsService.totalObservations(userId);
long distinctBodies = statsService.distinctBodiesObserved(userId);
long totalEquip = statsService.totalEquipmentUsed(userId);

summaryLabel.setText(totalObs + " 次观测 / " + distinctBodies
    + " 个星体 / " + totalEquip + " 件器材");
```

### 3.9 LoginFrame — i18n 完整接入

**文件：** `src/main/java/com/astrolog/ui/frame/LoginFrame.java`

**修改：** 所有硬编码中文按钮/标签文字改为从 ResourceBundle 读取：

```java
private static final ResourceBundle i18n =
    ResourceBundle.getBundle("i18n.messages_zh");

// ...
loginButton.setText(i18n.getString("login.button"));
registerButton.setText(i18n.getString("login.register"));
titleLabel.setText(i18n.getString("app.title"));
```

MainFrame 的菜单栏和 About 对话框同理。

### 3.10 StarMapCanvas — 扩展至 88 星座 + 全部星体（S8 遗留）

**文件：** `src/main/java/com/astrolog/ui/component/StarMapCanvas.java`（修改）

**改动内容：**

1. **constellations.json 增补连线数据：** 每星座新增 `lines` 字段——亮星间连线的坐标段数组：

   ```json
   {
     "name": "猎户座",
     "lines": [
       { "fromRA": 5.23, "fromDec": 6.3, "toRA": 5.53, "toDec": -1.2 },
       { "fromRA": 5.53, "fromDec": -1.2, "toRA": 5.6, "toDec": -5.4 }
     ]
   }
   ```

   88 星座共约 350-500 条连线。由新对话 Claude 生成。

2. **StarMapCanvas 加载方式变更：**
   - 删除硬编码的 15 星座静态数组
   - 改为通过 `JsonDataLoader.loadList("data/constellations.json", ConstellationStarData[].class)` 加载（需要一个含 lines 字段的新数据类，或复用 ConstellationInfo 并扩充）
   - 构造器签名扩展：`StarMapCanvas(List<CelestialBody> observed, List<CelestialBody> all, List<ConstellationStarData> constellations)`

3. **新增星体位置标记：**
   - 从 `BodyDao.findAll()` 读取所有星体
   - `drawAllBodies()` 方法：为所有星体绘制灰色小圆点（半径 2px）
   - 已观测星体绘制为亮色大圆点（半径 5px，金黄色）
   - 超过亮度阈值（mag < 3.0）的亮星标注名称

### 3.11 MessierPanel — 双击跳转星体库（S8 遗留）

**文件：** `src/main/java/com/astrolog/ui/panel/MessierPanel.java`（修改）

在 JTable 的 MouseListener 中：双击非已观测行时，弹窗提示"该天体尚未观测"；双击已观测行时：

```java
mainFrame.switchTo("星体库");
// CelestialBodyPanel 新增方法:
celestialBodyPanel.highlightBody(bodyId); // 滚动到该行并选中高亮
```

CelestialBodyPanel 新增：

```java
public void highlightBody(int bodyId) {
    // 在 JTable 中查找 bodyId 对应的行并滚动 + 选中
}
```

---

## 4. S9 交付物：项目最终产出

### 4.1 用户操作手册

**文件：** `docs/用户操作手册.md`（Markdown 格式）

内容大纲：

1. 系统概述
2. 安装与启动（JDK 17 + MySQL 8.0 + 建库 + 启动）
3. 注册与登录
4. 星体库浏览与管理
5. 观测记录添加与查询
6. 器材管理与维护日志
7. 统计图表与可视化
8. 今夜星空推荐
9. 梅西耶马拉松追踪
10. 星座文化馆
11. 报告导出（HTML/PDF）
12. 系统设置（主题/字体/备份恢复）

### 4.2 安装部署指南

**文件：** `docs/安装部署指南.md`

内容：

1. 环境要求（JDK 17+、MySQL 8.0+、Maven 3.8+）
2. 数据库初始化（`mysql -u root -p < sql/init.sql`）
3. 配置文件修改（`db.properties`）
4. 编译打包（`mvn clean package`）
5. 启动运行（`java -jar target/astrolog.jar`）

### 4.3 Fat JAR 打包

在 `pom.xml` 中配置 `maven-assembly-plugin` 生成含所有依赖的可执行 JAR：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.astrolog.AppMain</mainClass>
            </manifest>
        </archive>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>single</goal></goals>
        </execution>
    </executions>
</plugin>
```

执行 `mvn clean package` 生成 `target/astrolog-1.0-SNAPSHOT-jar-with-dependencies.jar`。

---

## 5. 测试规格

### 5.1 ExportServiceTest（8 例）

```
@Mock StatsService; @Mock MessierService; @InjectMocks ExportService

ES-001: generateHtmlReport → 文件创建且非空
ES-002: generateHtmlReport → HTML 包含用户名
ES-003: generateHtmlReport → HTML 包含观测次数
ES-004: generatePdfReport → 文件创建且非空（跳过 JasperReports 编译，Mock ExportUtil）
ES-005: generateMessierCert_success → isCertEligible true → 文件创建
ES-006: generateMessierCert_notEligible → isCertEligible false → 抛出异常
ES-007: generateHtmlReport_emptyData → 观测次数 0 → HTML 仍正常生成
ES-008: 输出目录自动创建（reports/）
```

---

## 6. 任务执行顺序

```
第 1 步 → ExportUtil.java（JasperReports 封装）
第 2 步 → ExportService.java（依赖 StatsService + MessierService + ExportUtil）
第 3 步 → BackupService.java（依赖 DBUtil）
第 4 步 → JasperReports 模板（annual_report.jrxml + messier_cert.jrxml）
第 5 步 → ReportViewDialog.java（依赖 ExportService）
第 6 步 → BackupRestoreDialog.java（依赖 BackupService）
第 7 步 → SettingsPanel.java（依赖 ThemeManager + BackupService）
第 8 步 → 修改 DashboardPanel.java（接入 StatsService 动态查询）
第 9 步 → 修改 LoginFrame.java（i18n ResourceBundle）
第 10 步 → 修改 MainFrame.java（系统设置 → SettingsPanel）
第 11 步 → 修改 StarMapCanvas.java（S8 遗留：扩展 88 星座连线 + 全部星体标记）
第 12 步 → 修改 MessierPanel.java + CelestialBodyPanel.java（S8 遗留：双击跳转星体库）
第 13 步 → ExportServiceTest.java
第 14 步 → 用户操作手册 + 安装部署指南
第 15 步 → maven-assembly-plugin 配置 + fat JAR 打包
```

---

## 7. 验收标准

| #   | 验收项                                    | 验证方法                      |
| --- | ----------------------------------------- | ----------------------------- |
| 1   | `mvn clean compile` 零错误                | Maven 编译                    |
| 2   | `mvn test` 全部通过（127 + 8 = 135 例）   | 执行测试                      |
| 3   | 生成 HTML 年度报告 → 浏览器可打开         | 手动操作 Statistics → 导出    |
| 4   | 生成 PDF 年度报告 → 内容正确              | 手动操作 + PDF 查看器         |
| 5   | 梅西耶证书（110/110 时可用）              | 模拟全部完成                  |
| 6   | 主题切换（暗色/亮色/星空）全局生效        | 手动操作 → 所有面板刷新       |
| 7   | 字体大小调节全局生效                      | 手动操作                      |
| 8   | 数据备份 → 生成 .sql 文件 → 内容完整      | 手动操作 + 检查文件           |
| 9   | 数据恢复 → 数据正确还原                   | 手动操作 + SQL 查询验证       |
| 10  | Dashboard 摘要数字为真实统计值            | 有观测记录时数字 > 0          |
| 11  | LoginFrame 按钮/标签从 i18n 读取          | 手动检查                      |
| 12  | StarMapCanvas 显示 88 星座连线（S8 遗留） | 打开星座分布图 → 全部星座可见 |
| 13  | StarMapCanvas 显示全部星体位置标记（S8）  | 灰色小圆点 + 已观测金黄色大点 |
| 14  | MessierPanel 双击跳转星体库（S8 遗留）    | 双击已观测天体 → 切换到星体库 |
| 15  | `mvn clean package` 生成可执行 fat JAR    | Maven 打包 + `java -jar` 启动 |

---

## 8. 注意事项

1. **JasperReports 中文支持：** 需要在模板中指定中文字体。推荐方案：在 `pom.xml` 添加 `com.itextpdf:itext-asian:5.2.0` 依赖，模板中使用 `pdfFontName="STSong-Light"`。
2. **BackupService.restore 事务：** 恢复操作用 `conn.setAutoCommit(false)` 包裹，失败时 rollback。但由于 SQL 分割方式简单（`;` 分行），复杂数据（note 含 `;`）可能分割错误。S9 接受此限制。
3. **备份恢复的 SwingWorker：** 备份操作可能耗时（大量 INSERT），必须在 `SwingWorker` 中执行，进度通过 `SwingWorker.publish()` 更新 UI。
4. **ThemeManager 观察者已就绪：** S1 已实现 ThemeManager + ThemeObserver 接口。SettingsPanel 只需调用 `ThemeManager.getInstance().setTheme()` 即可。
5. **DashboardPanel 动态化注意刷新：** 初始加载时查询一次 StatsService。后续在每次面板切换回仪表盘时触发刷新（可覆写 `addNotify()` 或 MainFrame 通知）。
6. **项目最终文件数目标：** S9 完成后预计 ~90+ Java 文件，~14,000 行代码，~135 测试。确认与原始设计目标（~100 文件，~14,100 行）接近。
7. **fat JAR 需排除测试类：** maven-assembly-plugin 默认可包含测试类，确认 `<scope>test</scope>` 依赖不被包含。
