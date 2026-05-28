# S4 — 器材管理 交接文档

> **子阶段：** S4 器材管理
> **所属阶段：** 阶段二 — 功能切片迭代
> **编制日期：** 2026-05-14
> **上级会话状态：** S3 已完成（57 Java 文件、52/52 测试通过），S4 无阻塞依赖

---

## 1. 前置状态速览

### 1.1 已交付资产

| 资产                        | 路径                              | S4 用途                                                                           |
| --------------------------- | --------------------------------- | --------------------------------------------------------------------------------- |
| `equipment` 表              | `sql/init.sql`                    | equip_id/user_id/name/type/aperture/focal_length/purchase_date/status/description |
| `equipment_maintenance` 表  | `sql/init.sql`                    | maint_id/equip_id/maint_date/description/cost/next_maint_date                     |
| `Equipment` 实体            | `model/Equipment.java`            | 10 字段完整映射，S4 直接使用                                                      |
| `EquipmentMaintenance` 实体 | `model/EquipmentMaintenance.java` | 6 字段完整映射                                                                    |
| `EquipType` 枚举            | `model/enums/EquipType.java`      | TELESCOPE/EYEPIECE/CAMERA/OTHER，fromString 已就绪                                |
| `EquipStatus` 枚举          | `model/enums/EquipStatus.java`    | ACTIVE/MAINTENANCE/RETIRED，fromString 已就绪                                     |
| `BaseDao<T>`                | `dao/BaseDao.java`                | executeQuery/executeUpdate/executeInsert                                          |
| `MainFrame`                 | `ui/frame/MainFrame.java`         | `getCurrentUserObject()` 返回 User；`switchTo(cardName)` 切换面板                 |

### 1.2 MainFrame 当前状态

NAV_LABELS[3] = "器材柜"，当前为占位 JPanel，需替换为 EquipmentPanel。S3 已替换"星体库"，S4 复用相同模式。

### 1.3 S2/S3 偏差经验（S4 必须遵循）

| 经验                                                   | S4 应用                                                                              |
| ------------------------------------------------------ | ------------------------------------------------------------------------------------ |
| COUNT 查询用 `rs.getInt(1) > 0` 而非 `!list.isEmpty()` | EquipDao.getUsageCount/openCount                                                     |
| executeInsert 仅限有自增主键的表                       | equipment 有自增 → 用 executeInsert；equipment_maintenance 有自增 → 用 executeInsert |
| UI 下拉中文 ↔ DB 英文转换                              | EquipType/EquipStatus 的 `fromString()` 已就绪，搜索筛选前调用                       |
| Dialog 构造器末尾必须 `setVisible(true)`               | EquipmentMaintDialog                                                                 |
| Service 需 package-private 测试构造器                  | EquipService(EquipDao, MaintDao)                                                     |
| ServiceResult 消息必须显式展示                         | EquipmentPanel 状态栏或 JOptionPane                                                  |
| getWindowAncestor() 获取父窗口                         | Dialog 需要刷新父面板时                                                              |

---

## 2. S4 目标与范围

### 2.1 核心目标

实现完整的个人器材管理：CRUD 器材主数据 + 维护日志记录 + 器材使用次数统计。

### 2.2 新增文件清单（8 个）

| #   | 文件                        | 包                                  | 行数 |
| --- | --------------------------- | ----------------------------------- | ---- |
| 1   | `EquipDao.java`             | `com.astrolog.dao`                  | ~180 |
| 2   | `MaintDao.java`             | `com.astrolog.dao`                  | ~100 |
| 3   | `EquipService.java`         | `com.astrolog.service`              | ~250 |
| 4   | `EquipmentPanel.java`       | `com.astrolog.ui.panel`             | ~320 |
| 5   | `EquipmentMaintDialog.java` | `com.astrolog.ui.dialog`            | ~150 |
| 6   | `EquipServiceTest.java`     | test `com.astrolog.unit.service`    | ~160 |
| 7   | `EquipDaoTest.java`         | test `com.astrolog.integration.dao` | ~130 |
| 8   | `MaintDaoTest.java`         | test `com.astrolog.integration.dao` | ~100 |

### 2.3 修改文件清单（1 个）

| #   | 文件             | 改动                              |
| --- | ---------------- | --------------------------------- |
| 1   | `MainFrame.java` | 导航"器材柜"占位 → EquipmentPanel |

### 2.4 范围边界

**不做：** 器材借用记录（obs_equipment 由 S5 观测记录管理）、兼容性矩阵图表（S6 可视化）、器材照片上传（S9）

---

## 3. 详细实现规格

### 3.1 EquipDao — 器材数据访问

**文件：** `src/main/java/com/astrolog/dao/EquipDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.Equipment;
import com.astrolog.model.enums.EquipStatus;
import com.astrolog.model.enums.EquipType;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class EquipDao extends BaseDao<Equipment> {

    // === 基础 CRUD ===

    public Equipment findById(int equipId) {
        String sql = "SELECT equip_id, user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description "
                   + "FROM equipment WHERE equip_id = ?";
        List<Equipment> results = executeQuery(sql, new Object[]{equipId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Equipment> findAllByUserId(int userId) {
        String sql = "SELECT equip_id, user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description "
                   + "FROM equipment WHERE user_id = ? ORDER BY name";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public int insert(Equipment equip) {
        String sql = "INSERT INTO equipment (user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            equip.getUserId(),
            equip.getName(),
            equip.getType().name().toLowerCase(),
            equip.getAperture(),
            equip.getFocalLength(),
            equip.getPurchaseDate() != null
                ? Date.valueOf(equip.getPurchaseDate()) : null,
            equip.getStatus().name().toLowerCase(),
            equip.getDescription()});
    }

    public boolean update(Equipment equip) {
        String sql = "UPDATE equipment SET name=?, type=?, aperture=?, "
                   + "focal_length=?, purchase_date=?, status=?, description=? "
                   + "WHERE equip_id=?";
        return executeUpdate(sql, new Object[]{
            equip.getName(),
            equip.getType().name().toLowerCase(),
            equip.getAperture(),
            equip.getFocalLength(),
            equip.getPurchaseDate() != null
                ? Date.valueOf(equip.getPurchaseDate()) : null,
            equip.getStatus().name().toLowerCase(),
            equip.getDescription(),
            equip.getEquipId()}) > 0;
    }

    public boolean delete(int equipId) {
        // 先清理关联的维护日志
        String delMaint = "DELETE FROM equipment_maintenance WHERE equip_id = ?";
        executeUpdate(delMaint, new Object[]{equipId});
        // 再删除器材
        String sql = "DELETE FROM equipment WHERE equip_id = ?";
        return executeUpdate(sql, new Object[]{equipId}) > 0;
    }

    // === 统计查询 ===

    // 器材使用次数（被关联的观测记录数）
    public int getUsageCount(int equipId) {
        String sql = "SELECT COUNT(*) FROM obs_equipment WHERE equip_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, equipId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);  // COUNT 始终有一行，直接取数值
            }
        } catch (SQLException e) {
            System.err.println("查询器材使用次数失败: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    // 按使用次数降序排列器材
    public List<Equipment> findAllSortedByUsage(int userId) {
        String sql = "SELECT e.equip_id, e.user_id, e.name, e.type, e.aperture, "
                   + "e.focal_length, e.purchase_date, e.status, e.description "
                   + "FROM equipment e "
                   + "LEFT JOIN obs_equipment oe ON e.equip_id = oe.equip_id "
                   + "WHERE e.user_id = ? "
                   + "GROUP BY e.equip_id "
                   + "ORDER BY COUNT(oe.obs_id) DESC";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    // === 枚举兼容：支持中文 displayName 查询（S3 教训） ===

    // 按名称模糊搜索
    public List<Equipment> searchByName(int userId, String keyword) {
        String sql = "SELECT equip_id, user_id, name, type, aperture, "
                   + "focal_length, purchase_date, status, description "
                   + "FROM equipment WHERE user_id = ? AND name LIKE ? "
                   + "ORDER BY name";
        return executeQuery(sql, new Object[]{userId, "%" + keyword + "%"}, this::mapRow);
    }

    // ResultSet 映射
    // 注意：设为 package-private，MaintDao 或其他类可能复用
    Equipment mapRow(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipId(rs.getInt("equip_id"));
        e.setUserId(rs.getInt("user_id"));
        e.setName(rs.getString("name"));
        e.setType(EquipType.fromString(rs.getString("type")));
        // aperture 可为 null
        BigDecimal ap = rs.getBigDecimal("aperture");
        e.setAperture(rs.wasNull() ? null : ap);
        e.setFocalLength(rs.getInt("focal_length"));
        Date pd = rs.getDate("purchase_date");
        e.setPurchaseDate(pd != null ? pd.toLocalDate() : null);
        e.setStatus(EquipStatus.fromString(rs.getString("status")));
        e.setDescription(rs.getString("description"));
        return e;
    }
}
```

**关键细节：**

- `getUsageCount` 用原生 JDBC（避开 BaseDao 模板），直接取 `rs.getInt(1)`；
- `delete` 先清理 `equipment_maintenance` 外键依赖；
- `findAllSortedByUsage` 用 LEFT JOIN + GROUP BY + COUNT 实现按使用频次排序；
- `mapRow` 为 package-private，与 S3 BodyDao 一致。

### 3.2 MaintDao — 维护日志数据访问

**文件：** `src/main/java/com/astrolog/dao/MaintDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.EquipmentMaintenance;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class MaintDao extends BaseDao<EquipmentMaintenance> {

    // 按器材 ID 查询所有维护记录
    public List<EquipmentMaintenance> findByEquipId(int equipId) {
        String sql = "SELECT maint_id, equip_id, maint_date, description, "
                   + "cost, next_maint_date "
                   + "FROM equipment_maintenance WHERE equip_id = ? "
                   + "ORDER BY maint_date DESC";
        return executeQuery(sql, new Object[]{equipId}, this::mapRow);
    }

    // 插入维护记录
    public int insert(EquipmentMaintenance m) {
        String sql = "INSERT INTO equipment_maintenance "
                   + "(equip_id, maint_date, description, cost, next_maint_date) "
                   + "VALUES (?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            m.getEquipId(),
            Date.valueOf(m.getMaintDate()),
            m.getDescription(),
            m.getCost(),
            m.getNextMaintDate() != null
                ? Date.valueOf(m.getNextMaintDate()) : null});
    }

    // 更新维护记录
    public boolean update(EquipmentMaintenance m) {
        String sql = "UPDATE equipment_maintenance SET maint_date=?, "
                   + "description=?, cost=?, next_maint_date=? "
                   + "WHERE maint_id=?";
        return executeUpdate(sql, new Object[]{
            Date.valueOf(m.getMaintDate()),
            m.getDescription(),
            m.getCost(),
            m.getNextMaintDate() != null
                ? Date.valueOf(m.getNextMaintDate()) : null,
            m.getMaintId()}) > 0;
    }

    // 删除维护记录
    public boolean delete(int maintId) {
        String sql = "DELETE FROM equipment_maintenance WHERE maint_id = ?";
        return executeUpdate(sql, new Object[]{maintId}) > 0;
    }

    // 检查器材是否有即将到期的维护（未来 30 天内）
    public List<EquipmentMaintenance> findUpcoming(int userId) {
        String sql = "SELECT em.maint_id, em.equip_id, em.maint_date, "
                   + "em.description, em.cost, em.next_maint_date "
                   + "FROM equipment_maintenance em "
                   + "JOIN equipment e ON em.equip_id = e.equip_id "
                   + "WHERE e.user_id = ? "
                   + "AND em.next_maint_date BETWEEN CURDATE() "
                   + "AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) "
                   + "ORDER BY em.next_maint_date";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    // ResultSet 映射
    private EquipmentMaintenance mapRow(ResultSet rs) throws SQLException {
        EquipmentMaintenance m = new EquipmentMaintenance();
        m.setMaintId(rs.getInt("maint_id"));
        m.setEquipId(rs.getInt("equip_id"));
        Date md = rs.getDate("maint_date");
        m.setMaintDate(md != null ? md.toLocalDate() : null);
        m.setDescription(rs.getString("description"));
        m.setCost(rs.getBigDecimal("cost"));
        Date nd = rs.getDate("next_maint_date");
        m.setNextMaintDate(nd != null ? nd.toLocalDate() : null);
        return m;
    }
}
```

### 3.3 EquipService — 器材业务逻辑

**文件：** `src/main/java/com/astrolog/service/EquipService.java`

```java
package com.astrolog.service;

import com.astrolog.dao.EquipDao;
import com.astrolog.dao.MaintDao;
import com.astrolog.model.Equipment;
import com.astrolog.model.EquipmentMaintenance;
import java.util.List;

public class EquipService {

    private final EquipDao equipDao;
    private final MaintDao maintDao;

    // 公开构造器
    public EquipService() {
        this.equipDao = new EquipDao();
        this.maintDao = new MaintDao();
    }

    // package-private 测试构造器（S2 偏差经验）
    EquipService(EquipDao equipDao, MaintDao maintDao) {
        this.equipDao = equipDao;
        this.maintDao = maintDao;
    }

    // ==================== 器材 CRUD ====================

    public ServiceResult addEquipment(Equipment equip) {
        if (equip.getName() == null || equip.getName().trim().isEmpty()) {
            return ServiceResult.fail("器材名称不能为空");
        }
        if (equip.getType() == null) {
            return ServiceResult.fail("器材类型不能为空");
        }
        int id = equipDao.insert(equip);
        if (id > 0) {
            return ServiceResult.success("器材已添加");
        }
        return ServiceResult.fail("添加失败");
    }

    public ServiceResult updateEquipment(Equipment equip) {
        if (equip.getName() == null || equip.getName().trim().isEmpty()) {
            return ServiceResult.fail("器材名称不能为空");
        }
        if (equipDao.update(equip)) {
            return ServiceResult.success("器材信息已更新");
        }
        return ServiceResult.fail("更新失败");
    }

    public ServiceResult deleteEquipment(int equipId) {
        if (equipDao.delete(equipId)) {
            return ServiceResult.success("器材已删除");
        }
        return ServiceResult.fail("删除失败");
    }

    // ==================== 查询 ====================

    public Equipment getEquipment(int equipId) {
        return equipDao.findById(equipId);
    }

    public List<Equipment> listByUser(int userId) {
        return equipDao.findAllByUserId(userId);
    }

    public List<Equipment> listByUsage(int userId) {
        return equipDao.findAllSortedByUsage(userId);
    }

    public List<Equipment> searchByName(int userId, String keyword) {
        return equipDao.searchByName(userId, keyword);
    }

    public int getUsageCount(int equipId) {
        return equipDao.getUsageCount(equipId);
    }

    // ==================== 维护日志管理 ====================

    public ServiceResult addMaintenance(EquipmentMaintenance m) {
        if (m.getEquipId() <= 0) {
            return ServiceResult.fail("器材 ID 无效");
        }
        if (m.getMaintDate() == null) {
            return ServiceResult.fail("维护日期不能为空");
        }
        int id = maintDao.insert(m);
        if (id > 0) {
            return ServiceResult.success("维护记录已添加");
        }
        return ServiceResult.fail("添加失败");
    }

    public ServiceResult updateMaintenance(EquipmentMaintenance m) {
        if (maintDao.update(m)) {
            return ServiceResult.success("维护记录已更新");
        }
        return ServiceResult.fail("更新失败");
    }

    public ServiceResult deleteMaintenance(int maintId) {
        if (maintDao.delete(maintId)) {
            return ServiceResult.success("维护记录已删除");
        }
        return ServiceResult.fail("删除失败");
    }

    public List<EquipmentMaintenance> getMaintenanceHistory(int equipId) {
        return maintDao.findByEquipId(equipId);
    }

    public List<EquipmentMaintenance> getUpcomingMaintenance(int userId) {
        return maintDao.findUpcoming(userId);
    }

    // ==================== 兼容性检查 ====================

    // 检查同一类型器材是否已存在（简单兼容性检查）
    public boolean hasSameType(int userId, String type) {
        List<Equipment> equipmentList = equipDao.findAllByUserId(userId);
        return equipmentList.stream()
            .anyMatch(e -> e.getType().name().equalsIgnoreCase(type));
    }
}
```

### 3.4 EquipmentPanel — 器材管理面板

**文件：** `src/main/java/com/astrolog/ui/panel/EquipmentPanel.java`

**布局结构（~320 行）：**

```
┌─────────────────────────────────────────────────┐
│ BorderLayout                                    │
│                                                 │
│ ┌─ 上部分：器材列表（CENTER）─────────────────┐ │
│ │                                             │ │
│ │ 工具栏: [关键字搜索] [按使用次数排序]        │ │
│ │         [添加器材]  [编辑]  [删除]           │ │
│ │                                             │ │
│ │ JTable (器材列表):                           │ │
│ │ 名称 | 类型 | 口径(mm) | 焦距(mm) | 状态 |   │ │
│ │      | 购买日期 | 使用次数                  │ │
│ │                                             │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ ┌─ 下部分：维护日志（SOUTH）──────────────────┐ │
│ │ TitledBorder("维护记录")                     │ │
│ │ [添加维护] [编辑] [删除]                     │ │
│ │ JTable: 日期 | 描述 | 费用 | 下次维护        │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public EquipmentPanel(User currentUser)`
2. 器材 JTable 使用 DefaultTableModel，列：`{"名称", "类型", "口径(mm)", "焦距(mm)", "状态", "购买日期", "使用次数"}`
3. 选中器材行后，下方维护日志自动刷新显示该器材的维护记录（行选择监听器）
4. "添加器材"弹出 JDialog 表单：名称、类型下拉（望远镜/目镜/相机/其他）、口径、焦距、购买日期、状态下拉（在用/维修/退役）、描述
5. "编辑"预填当前选中行的数据
6. "删除"弹出确认对话框
7. 维护日志子表的"添加维护"弹出 EquipmentMaintDialog，创建后刷新子表
8. 维护日志的"编辑/删除"操作对象为当前选中维护记录行
9. 器材使用次数列显示 `equipService.getUsageCount(equipId)` 结果
10. 权限控制：器材所有者可见编辑/删除按钮（通过 `currentUser.getUserId() == equipment.getUserId()` 判断），维护日志同理
11. 类型下拉显示中文（EquipType.getDisplayName()），参与搜索时用 fromString 转换
12. 面板加载时自动加载当前用户的所有器材

### 3.5 EquipmentMaintDialog — 维护记录对话框

**文件：** `src/main/java/com/astrolog/ui/dialog/EquipmentMaintDialog.java`

**布局（~150 行）：**

```java
public class EquipmentMaintDialog extends JDialog {

    public EquipmentMaintDialog(JFrame parent, int equipId,
                                 EquipmentMaintenance existing,  // null=新增
                                 Runnable onSaved) {
        super(parent, "维护记录", true);
        // ... 表单组件：维护日期(JTextField或JDatePicker替代)、
        //     描述(JTextArea)、费用(JTextField)、下次维护日期(JTextField)
        // ... [保存] [取消] 按钮

        // 保存按钮事件:
        //   1. 收集字段 → 构造 EquipmentMaintenance
        //   2. 调用 equipService.addMaintenance() 或 updateMaintenance()
        //   3. 如果成功，JOptionPane.showMessageDialog() 显示消息
        //   4. onSaved.run() 回调刷新父面板
        //   5. dispose()

        // 【重要】构造器末尾必须:
        setVisible(true);
    }
}
```

**构造器参数说明：**

- `JFrame parent` — 父窗口，通过 `getWindowAncestor()` 获取（S3 偏差经验）
- `int equipId` — 关联的器材 ID
- `EquipmentMaintenance existing` — null 表示新增模式，非 null 表示编辑模式（预填值）
- `Runnable onSaved` — 保存成功后的回调，用于刷新维护日志子表

---

## 4. MainFrame 修改

**文件：** `src/main/java/com/astrolog/ui/frame/MainFrame.java`

S3 后的 MainFrame 已有 CelestialBodyPanel 替换逻辑。S4 只需在相同位置增加 EquipmentPanel 替换：

```java
// 在 CelestialBodyPanel 之后（或按已建立的模式），加入:
if (NAV_LABELS[i].equals("器材柜")) {
    EquipmentPanel equipPanel = new EquipmentPanel(currentUser);
    contentPanel.add(equipPanel, "器材柜");
}
```

同时导入 `com.astrolog.ui.panel.EquipmentPanel`。

---

## 5. 测试规格

### 5.1 EquipServiceTest（10 例）

**文件：** `src/test/java/com/astrolog/unit/service/EquipServiceTest.java`

```
@ExtendWith(MockitoExtension.class)
@Mock EquipDao, @Mock MaintDao, @InjectMocks EquipService

ES-001: addEquipment_success()          — 正常添加
ES-002: addEquipment_emptyName()        — 空名称被拒
ES-003: addEquipment_nullType()         — 空类型被拒
ES-004: updateEquipment_success()       — 正常更新
ES-005: deleteEquipment_success()       — 正常删除
ES-006: addMaintenance_success()        — 正常添加维护记录
ES-007: addMaintenance_invalidEquip()   — 无效器材 ID 被拒
ES-008: addMaintenance_nullDate()       — 空日期被拒
ES-009: getUsageCount()                 — 使用次数正确
ES-010: getUpcomingMaintenance()        — 即将到期维护查询
```

### 5.2 EquipDaoTest（6 例）

**文件：** `src/test/java/com/astrolog/integration/dao/EquipDaoTest.java`

```
@BeforeEach: 通过 UserDao 创建测试用户，DELETE 测试器材数据

IT-ED-001: insert → findById → 所有字段值一致
IT-ED-002: insert → update 改名称+状态 → findById → 更新正确
IT-ED-003: insert → delete → findById → null
IT-ED-004: insert 3 个器材 → findAllByUserId → 返回 3 条
IT-ED-005: searchByName → 模糊搜索命中
IT-ED-006: findAllSortedByUsage → 按 LEFT JOIN COUNT 排序正确
```

### 5.3 MaintDaoTest（4 例）

**文件：** `src/test/java/com/astrolog/integration/dao/MaintDaoTest.java`

```
@BeforeEach: 通过 UserDao + EquipDao 创建测试用户和测试器材

IT-MD-001: insert → findByEquipId → 内容一致
IT-MD-002: insert → update → findByEquipId → 更新正确
IT-MD-003: insert → delete → findByEquipId → 空列表
IT-MD-004: insert 2 条不同日期 → findUpcoming → 只返回未来 30 天内的
```

---

## 6. 任务执行顺序

```
第 1 步 → EquipDao.java（依赖 BaseDao, EquipType, EquipStatus）
第 2 步 → MaintDao.java（依赖 BaseDao）
第 3 步 → EquipService.java（依赖 EquipDao + MaintDao）
    注意：必须保留 package-private EquipService(EquipDao, MaintDao) 测试构造器
第 4 步 → EquipmentPanel.java（依赖 EquipService）
第 5 步 → EquipmentMaintDialog.java（依赖 EquipService）
第 6 步 → 修改 MainFrame.java（"器材柜"占位 → EquipmentPanel）
第 7 步 → EquipServiceTest.java
第 8 步 → EquipDaoTest.java + MaintDaoTest.java（需 MySQL）
```

---

## 7. 验收标准

| #   | 验收项                                                      | 验证方法                       |
| --- | ----------------------------------------------------------- | ------------------------------ |
| 1   | `mvn clean compile` 零错误                                  | Maven 编译                     |
| 2   | `mvn test` 全部通过（S1 7 + S2 20 + S3 25 + S4 20 = 72 例） | 执行测试                       |
| 3   | 器材列表正确显示（含使用次数列）                            | 启动应用 → 器材柜              |
| 4   | 添加/编辑/删除器材正常                                      | 手动操作表单+确认              |
| 5   | 维护日志添加/编辑/删除正常                                  | 选中器材 → 维护记录子表操作    |
| 6   | 按使用次数排序功能正常                                      | 点击排序按钮                   |
| 7   | 关键字搜索器材功能正常                                      | 搜索框输入                     |
| 8   | 删除器材时关联维护日志被级联清理                            | 删除器材 → 检查 maintenance 表 |
| 9   | 器材所有者可见操作按钮                                      | 权限验证                       |
| 10  | 即将到期维护提示正常                                        | 手动验证 MaintDao.findUpcoming |

---

## 8. 注意事项

1. **executeInsert vs executeUpdate：** `equipment` 和 `equipment_maintenance` 均有自增主键，使用 `executeInsert` 获取生成 ID。
2. **类型/状态 DB 值：** DB 存储小写英文（`telescope`、`active`），UI 显示中文。EquipType/EquipStatus 的 fromString() 方法已支持双向匹配（name + displayName）。
3. **Dialog 必须 setVisible(true)：** S3 的 ImportCsvDialog 漏了导致无响应，S4 所有 Dialog 构造器末尾必须调用，并在交接文档中标注。
4. **getUsageCount 实现：** 不要用 executeQuery + isEmpty() 判断（S3 偏差：COUNT 始终返回一行）。直接用原生 JDBC 取 rs.getInt(1)。
5. **delete 级联清理：** equipment_maintenance 有 FK 到 equipment（ON DELETE CASCADE 应在 init.sql 中定义）。如果 FK 未定义 CASCADE，需在代码中手动清理。S4 在 EquipDao.delete 中显式先删维护日志（双重保险）。
6. **EquipDao.mapRow 设为 package-private：** 与其他 DAO（如后续的 ObsDao 需要关联查询器材）共享映射逻辑。
