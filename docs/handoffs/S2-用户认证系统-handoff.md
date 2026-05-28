# S2 — 用户认证系统 交接文档

> **子阶段：** S2 用户认证系统
> **所属阶段：** 阶段一 — 基础奠基
> **编制日期：** 2026-05-14
> **上级会话状态：** S1 已完成验收（42 文件、7 测试通过、11 表就绪），已知问题已修复（JaCoCo 执行绑定已移除、Validator 拆分为 3 个 public 类、BaseDao 表名白名单已添加）

---

## 1. 前置状态速览

### 1.1 S1 已交付资产

| 资产                | 路径                           | S2 如何用                                                                                |
| ------------------- | ------------------------------ | ---------------------------------------------------------------------------------------- |
| `users` 表          | `sql/init.sql`                 | 含全部字段（username/password/role/locked_until/login_attempts/last_login），S2 直接读写 |
| `operation_logs` 表 | `sql/init.sql`                 | user_id / operation / detail / ip_address / create_time，S2 写入登录/注册/管理操作日志   |
| `BaseDao<T>`        | `dao/BaseDao.java`             | 提供 executeQuery/executeUpdate/executeInsert/closeResources，UserDao 和 LogDao 继承     |
| `PasswordUtil`      | `util/PasswordUtil.java`       | `hash(plain)` → BCrypt 哈希 / `verify(plain, hashed)` → boolean                          |
| `Validator`         | `util/Validator.java`          | `usernameValidator()` 和 `passwordValidator()` 静工工厂方法                              |
| `DBUtil`            | `util/DBUtil.java`             | 单例连接池，`getInstance().getConnection()` 获取连接                                     |
| `LoginFrame`        | `ui/frame/LoginFrame.java`     | 登录/注册按钮事件为占位桩 → S2 替换                                                      |
| `MainFrame`         | `ui/frame/MainFrame.java`      | 构造器当前接收 `(String username, String role)` → 改为 `(User user)`                     |
| `DashboardPanel`    | `ui/panel/DashboardPanel.java` | 欢迎语静态占位 → 显示真实用户信息                                                        |
| `User` 实体         | `model/User.java`              | 全部字段 + getter/setter，与 users 表一一对应                                            |
| `OperationLog` 实体 | `model/OperationLog.java`      | userId/operation/detail/ipAddress/createTime                                             |
| `UserRole` 枚举     | `model/enums/UserRole.java`    | OBSERVER("observer") / ADMIN("admin")                                                    |

### 1.2 S1 待修改代码的当前状态

**LoginFrame.java** 中两个关键方法的当前代码（第 127-136 行）：

```java
private void onLogin(ActionEvent e) {
    System.out.println("登录功能将在S2实现");
    MainFrame mainFrame = new MainFrame("测试用户", "observer");
    mainFrame.setVisible(true);
    dispose();
}

private void onRegister(ActionEvent e) {
    System.out.println("注册功能将在S2实现");
}
```

**MainFrame.java** 当前构造器签名和字段（需要修改的位置）：

```java
private String currentUsername;  // S2 删除，改为 private User currentUser;
private String currentUserRole;  // S2 删除

public MainFrame(String username, String role) {  // S2 改为 public MainFrame(User user)
    this.currentUsername = username;
    this.currentUserRole = role;
    // 状态栏中 setText(username + " | " + role)
}
```

**DashboardPanel.java** 当前构造器签名（需要修改的位置）：

```java
public DashboardPanel() {  // S2 改为 public DashboardPanel(User user)
    // 欢迎语为静态文字 "欢迎使用 AstroLog"
}
```

---

## 2. S2 目标与范围

### 2.1 一句话目标

将用户认证从占位桩变为完整闭环：可注册 → 可登录（含锁定） → 可维护个人信息 → 管理员可管理用户。所有 UI 接入真实 Service 层逻辑。

### 2.2 新增文件清单（10 个）

| #   | 文件                   | 包                                  | 行数 |
| --- | ---------------------- | ----------------------------------- | ---- |
| 1   | `UserDao.java`         | `com.astrolog.dao`                  | ~200 |
| 2   | `LogDao.java`          | `com.astrolog.dao`                  | ~120 |
| 3   | `UserService.java`     | `com.astrolog.service`              | ~300 |
| 4   | `ServiceResult.java`   | `com.astrolog.service`              | ~30  |
| 5   | `LoginResult.java`     | `com.astrolog.service`              | ~35  |
| 6   | `RegisterDialog.java`  | `com.astrolog.ui.dialog`            | ~200 |
| 7   | `UserPanel.java`       | `com.astrolog.ui.panel`             | ~350 |
| 8   | `UserServiceTest.java` | test `com.astrolog.unit.service`    | ~250 |
| 9   | `UserDaoTest.java`     | test `com.astrolog.integration.dao` | ~150 |
| 10  | `LogDaoTest.java`      | test `com.astrolog.integration.dao` | ~100 |

### 2.3 修改文件清单（3 个）

| #   | 文件                  | 修改量                                                |
| --- | --------------------- | ----------------------------------------------------- |
| 1   | `LoginFrame.java`     | ~150 行（onLogin/onRegister 替换为真实逻辑）          |
| 2   | `MainFrame.java`      | ~30 行（构造器参数类型变更 + 状态栏 + 导航传递 user） |
| 3   | `DashboardPanel.java` | ~20 行（构造器加参数，欢迎语动态化）                  |

### 2.4 范围边界

**不做：** 头像上传（留桩）、账号注销、邮件通知、非用户模块的任何 DAO/Service/Panel

---

## 3. 详细实现规格

### 3.1 UserDao — 用户数据访问

**文件：** `src/main/java/com/astrolog/dao/UserDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao extends BaseDao<User> {

    // === 查询方法 ===

    public User findByUsername(String username) {
        String sql = "SELECT user_id, username, password, role, avatar_path, "
                   + "city, default_lat, default_lon, login_attempts, "
                   + "locked_until, last_login, create_time "
                   + "FROM users WHERE username = ?";
        List<User> results = executeQuery(sql, new Object[]{username}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public User findById(int userId) {
        String sql = "SELECT user_id, username, password, role, avatar_path, "
                   + "city, default_lat, default_lon, login_attempts, "
                   + "locked_until, last_login, create_time "
                   + "FROM users WHERE user_id = ?";
        List<User> results = executeQuery(sql, new Object[]{userId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<User> findAll() {
        String sql = "SELECT user_id, username, password, role, avatar_path, "
                   + "city, default_lat, default_lon, login_attempts, "
                   + "locked_until, last_login, create_time "
                   + "FROM users ORDER BY create_time DESC";
        return executeQuery(sql, null, this::mapRow);
    }

    // === 写操作 ===

    public int insert(User user) {
        String sql = "INSERT INTO users (username, password, role, city, "
                   + "default_lat, default_lon) VALUES (?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            user.getUsername(), user.getPassword(), user.getRole().name().toLowerCase(),
            user.getCity(), user.getDefaultLat(), user.getDefaultLon()});
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET city=?, default_lat=?, default_lon=?, "
                   + "avatar_path=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{
            user.getCity(), user.getDefaultLat(), user.getDefaultLon(),
            user.getAvatarPath(), user.getUserId()}) > 0;
    }

    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{hashedPassword, userId}) > 0;
    }

    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id=?";
        return executeUpdate(sql, new Object[]{userId}) > 0;
    }

    // === 登录安全相关 ===

    public boolean updateLoginAttempts(int userId, int attempts) {
        String sql = "UPDATE users SET login_attempts=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{attempts, userId}) > 0;
    }

    public boolean updateLockedUntil(int userId, LocalDateTime lockedUntil) {
        String sql = "UPDATE users SET locked_until=? WHERE user_id=?";
        return executeUpdate(sql, new Object[]{
            lockedUntil != null ? Timestamp.valueOf(lockedUntil) : null, userId}) > 0;
    }

    public boolean updateLastLogin(int userId, LocalDateTime time) {
        String sql = "UPDATE users SET last_login=?, login_attempts=0, locked_until=NULL WHERE user_id=?";
        return executeUpdate(sql, new Object[]{Timestamp.valueOf(time), userId}) > 0;
    }

    // === ResultSet 映射 ===

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(UserRole.fromString(rs.getString("role")));
        u.setAvatarPath(rs.getString("avatar_path"));
        u.setCity(rs.getString("city"));
        BigDecimal lat = rs.getBigDecimal("default_lat");
        u.setDefaultLat(lat);
        BigDecimal lon = rs.getBigDecimal("default_lon");
        u.setDefaultLon(lon);
        u.setLoginAttempts(rs.getInt("login_attempts"));
        Timestamp lockedTs = rs.getTimestamp("locked_until");
        u.setLockedUntil(lockedTs != null ? lockedTs.toLocalDateTime() : null);
        Timestamp lastTs = rs.getTimestamp("last_login");
        u.setLastLogin(lastTs != null ? lastTs.toLocalDateTime() : null);
        Timestamp createTs = rs.getTimestamp("create_time");
        u.setCreateTime(createTs != null ? createTs.toLocalDateTime() : null);
        return u;
    }
}
```

**关键细节：**

- `UserRole.fromString()` 需要在枚举中实现：遍历 `values()` 匹配 `displayName`；
- INSERT 时不写入 `login_attempts` / `locked_until` / `last_login` / `create_time`，全部依赖数据库 DEFAULT；
- `updateLastLogin` 一次性重置 attempts=0 + locked_until=NULL + last_login=NOW；
- 所有 SQL 显式列出列名，禁用 `SELECT *`。

### 3.2 LogDao — 操作日志数据访问

**文件：** `src/main/java/com/astrolog/dao/LogDao.java`

```java
package com.astrolog.dao;

import com.astrolog.model.OperationLog;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class LogDao extends BaseDao<OperationLog> {

    public int insert(OperationLog log) {
        String sql = "INSERT INTO operation_logs (user_id, operation, detail, ip_address) "
                   + "VALUES (?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            log.getUserId(), log.getOperation(), log.getDetail(), log.getIpAddress()});
    }

    public List<OperationLog> findByUserId(int userId) {
        String sql = "SELECT log_id, user_id, operation, detail, ip_address, create_time "
                   + "FROM operation_logs WHERE user_id = ? ORDER BY create_time DESC";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public List<OperationLog> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT log_id, user_id, operation, detail, ip_address, create_time "
                   + "FROM operation_logs WHERE create_time BETWEEN ? AND ? "
                   + "ORDER BY create_time DESC";
        return executeQuery(sql, new Object[]{
            Timestamp.valueOf(start), Timestamp.valueOf(end)}, this::mapRow);
    }

    private OperationLog mapRow(ResultSet rs) throws SQLException {
        OperationLog log = new OperationLog();
        log.setLogId(rs.getInt("log_id"));
        log.setUserId(rs.getInt("user_id"));
        log.setOperation(rs.getString("operation"));
        log.setDetail(rs.getString("detail"));
        log.setIpAddress(rs.getString("ip_address"));
        Timestamp ts = rs.getTimestamp("create_time");
        log.setCreateTime(ts != null ? ts.toLocalDateTime() : null);
        return log;
    }
}
```

### 3.3 ServiceResult / LoginResult — 服务层返回类型

**文件：** `src/main/java/com/astrolog/service/ServiceResult.java`

```java
package com.astrolog.service;

public class ServiceResult {
    private final boolean success;
    private final String message;

    private ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ServiceResult success(String msg) { return new ServiceResult(true, msg); }
    public static ServiceResult fail(String msg) { return new ServiceResult(false, msg); }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
```

**文件：** `src/main/java/com/astrolog/service/LoginResult.java`

```java
package com.astrolog.service;

import com.astrolog.model.User;

public class LoginResult {
    private final boolean success;
    private final String message;
    private final User user;      // 仅 success=true 时非 null

    private LoginResult(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public static LoginResult success(User user) { return new LoginResult(true, "登录成功", user); }
    public static LoginResult fail(String msg) { return new LoginResult(false, msg, null); }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public User getUser() { return user; }
}
```

### 3.4 UserService — 认证业务逻辑（核心类）

**文件：** `src/main/java/com/astrolog/service/UserService.java`

```java
package com.astrolog.service;

import com.astrolog.dao.UserDao;
import com.astrolog.dao.LogDao;
import com.astrolog.model.User;
import com.astrolog.model.OperationLog;
import com.astrolog.model.enums.UserRole;
import com.astrolog.util.PasswordUtil;
import com.astrolog.util.Validator;
import com.astrolog.util.ValidationResult;
import com.astrolog.config.AppConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserService {

    private final UserDao userDao;
    private final LogDao logDao;

    public UserService() {
        this.userDao = new UserDao();
        this.logDao = new LogDao();
    }

    // ==================== 注册 ====================

    public ServiceResult register(String username, String password,
                                   String role, String city,
                                   BigDecimal lat, BigDecimal lon) {
        // 1. 输入校验
        ValidationResult vr = Validator.usernameValidator().validateFirst(username);
        if (!vr.isValid()) return ServiceResult.fail(vr.getMessage());

        vr = Validator.passwordValidator().validateFirst(password);
        if (!vr.isValid()) return ServiceResult.fail(vr.getMessage());

        // 2. 重名检测
        if (userDao.findByUsername(username) != null) {
            return ServiceResult.fail("用户名已存在");
        }

        // 3. 构造用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hash(password));   // BCrypt 加密
        user.setRole(UserRole.fromString(role));
        user.setCity(city);
        user.setDefaultLat(lat);
        user.setDefaultLon(lon);

        // 4. 写入数据库
        int userId = userDao.insert(user);
        if (userId <= 0) {
            return ServiceResult.fail("注册失败，请重试");
        }

        // 5. 操作日志
        writeLog(userId, "注册", "新用户注册: " + username);

        return ServiceResult.success("注册成功");
    }

    // ==================== 登录 ====================

    public LoginResult login(String username, String password) {
        // 1. 查询用户
        User user = userDao.findByUsername(username);
        if (user == null) {
            return LoginResult.fail("用户名或密码错误");
        }

        // 2. 检查锁定
        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutes = ChronoUnit.MINUTES.between(
                LocalDateTime.now(), user.getLockedUntil());
            return LoginResult.fail(String.format(
                "账号已锁定，请于 %d 分钟后重试", Math.max(1, minutes)));
        }

        // 3. 验证密码
        if (!PasswordUtil.verify(password, user.getPassword())) {
            int newAttempts = user.getLoginAttempts() + 1;
            userDao.updateLoginAttempts(user.getUserId(), newAttempts);

            if (newAttempts >= AppConfig.MAX_LOGIN_ATTEMPTS) {
                LocalDateTime lockUntil = LocalDateTime.now()
                    .plusMinutes(AppConfig.LOCK_DURATION_MINUTES);
                userDao.updateLockedUntil(user.getUserId(), lockUntil);
                writeLog(user.getUserId(), "登录锁定",
                    "连续" + newAttempts + "次失败，锁定至 " + lockUntil);
                return LoginResult.fail("账号已锁定，请于 "
                    + AppConfig.LOCK_DURATION_MINUTES + " 分钟后重试");
            }

            return LoginResult.fail("用户名或密码错误");
        }

        // 4. 登录成功
        LocalDateTime now = LocalDateTime.now();
        userDao.updateLastLogin(user.getUserId(), now);
        user.setLastLogin(now);
        user.setLoginAttempts(0);
        user.setLockedUntil(null);

        writeLog(user.getUserId(), "登录", "用户登录成功");

        return LoginResult.success(user);
    }

    // ==================== 个人信息维护 ====================

    public ServiceResult updateProfile(int userId, String city,
                                        BigDecimal lat, BigDecimal lon) {
        // 坐标范围校验
        if (lat != null && (lat.compareTo(new BigDecimal("-90")) < 0
                || lat.compareTo(new BigDecimal("90")) > 0)) {
            return ServiceResult.fail("纬度必须在 -90 到 90 之间");
        }
        if (lon != null && (lon.compareTo(new BigDecimal("-180")) < 0
                || lon.compareTo(new BigDecimal("180")) > 0)) {
            return ServiceResult.fail("经度必须在 -180 到 180 之间");
        }

        User user = userDao.findById(userId);
        if (user == null) return ServiceResult.fail("用户不存在");

        user.setCity(city);
        user.setDefaultLat(lat);
        user.setDefaultLon(lon);

        if (userDao.update(user)) {
            return ServiceResult.success("个人信息已更新");
        }
        return ServiceResult.fail("更新失败，请重试");
    }

    // ==================== 修改密码 ====================

    public ServiceResult changePassword(int userId, String oldPassword,
                                         String newPassword) {
        // 1. 新密码强度校验
        ValidationResult vr = Validator.passwordValidator().validateFirst(newPassword);
        if (!vr.isValid()) return ServiceResult.fail(vr.getMessage());

        // 2. 原密码校验
        User user = userDao.findById(userId);
        if (user == null) return ServiceResult.fail("用户不存在");
        if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
            return ServiceResult.fail("原密码错误");
        }

        // 3. 更新密码
        String hashed = PasswordUtil.hash(newPassword);
        if (userDao.updatePassword(userId, hashed)) {
            writeLog(userId, "修改密码", "密码已更新");
            return ServiceResult.success("密码修改成功");
        }
        return ServiceResult.fail("密码修改失败，请重试");
    }

    // ==================== 管理员功能 ====================

    public List<User> listAllUsers() {
        return userDao.findAll();
    }

    public ServiceResult setUserEnabled(int userId, boolean enabled) {
        if (enabled) {
            userDao.updateLockedUntil(userId, null);
            userDao.updateLoginAttempts(userId, 0);
            writeLog(userId, "启用账号", "");
            return ServiceResult.success("账号已启用");
        } else {
            // 用远未来日期模拟永久禁用
            userDao.updateLockedUntil(userId, LocalDateTime.of(2099, 12, 31, 23, 59));
            writeLog(userId, "禁用账号", "");
            return ServiceResult.success("账号已禁用");
        }
    }

    public ServiceResult resetPassword(int userId, String newPassword) {
        ValidationResult vr = Validator.passwordValidator().validateFirst(newPassword);
        if (!vr.isValid()) return ServiceResult.fail(vr.getMessage());

        String hashed = PasswordUtil.hash(newPassword);
        if (userDao.updatePassword(userId, hashed)) {
            writeLog(userId, "重置密码", "管理员重置密码");
            return ServiceResult.success("密码已重置");
        }
        return ServiceResult.fail("重置失败，请重试");
    }

    // ==================== 内部辅助 ====================

    private void writeLog(int userId, String operation, String detail) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperation(operation);
        log.setDetail(detail);
        log.setIpAddress("127.0.0.1");
        log.setCreateTime(LocalDateTime.now());
        logDao.insert(log);
    }
}
```

**重要约束：**

- `register` 的 role 参数来自 UI 下拉框（"observer" 或 "admin" 的中文显示名），需用 `UserRole.fromString()` 转换；
- `login` 成功时直接在内存更新 user 对象的 attempts/lockedUntil/lastLogin，返回给 UI 的是最新状态，避免 UI 需要再次查询；
- `setUserEnabled(false)` 用远未来日期模拟"永久禁用"（不用额外字段）。

### 3.5 RegisterDialog — 注册对话框

**文件：** `src/main/java/com/astrolog/ui/dialog/RegisterDialog.java`

设计要点（~200 行）：

```
┌──────────────────────────────────────┐
│ 用户注册                             │
├──────────────────────────────────────┤
│                                      │
│ 用户名:     [___________________]   │
│ 密码:       [___________________]   │
│ 确认密码:   [___________________]   │
│ 角色:       [观测者 ▼]              │
│ 所在城市:   [___________________]   │
│ 默认纬度:   [___________________]   │
│ 默认经度:   [___________________]   │
│                                      │
│          [注册]    [取消]            │
│                                      │
│ 提示: 带 * 为必填项                   │
└──────────────────────────────────────┘
```

**实现关键：**

- extends JDialog，模态 (true)
- JComboBox<String> 角色下拉：["观测者", "管理员"]，选中值转为 "observer"/"admin"
- 注册按钮事件中：收集各字段 → 两次密码一致性检查 → 调用 `new UserService().register(...)` → 成功则 dispose 并提示 → 失败则 JOptionPane 显示错误
- 取消按钮 dispose 不做任何事
- 纬度/经度输入验证（可为空，非空时校验数值范围）
- 注册成功后关闭对话框，回到 LoginFrame 并提示"注册成功，请登录"
- 设置默认关闭操作为 DISPOSE_ON_CLOSE

### 3.6 LoginFrame — 接入真实逻辑

**文件：** `src/main/java/com/astrolog/ui/frame/LoginFrame.java`

**onLogin 新实现（替换第 127-132 行）：**

```java
private void onLogin(ActionEvent e) {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "请输入用户名和密码",
            "提示", JOptionPane.WARNING_MESSAGE);
        return;
    }

    UserService userService = new UserService();
    LoginResult result = userService.login(username, password);

    if (result.isSuccess()) {
        MainFrame mainFrame = new MainFrame(result.getUser());
        mainFrame.setVisible(true);
        dispose();
    } else {
        JOptionPane.showMessageDialog(this, result.getMessage(),
            "登录失败", JOptionPane.ERROR_MESSAGE);
    }
}
```

**onRegister 新实现（替换第 134-136 行）：**

```java
private void onRegister(ActionEvent e) {
    RegisterDialog dialog = new RegisterDialog(this);
    dialog.setVisible(true);  // 模态，阻塞直到关闭
    // 如果注册成功，RegisterDialog 内部已弹出成功提示
}
```

**新增 import：** `com.astrolog.service.UserService`, `com.astrolog.service.LoginResult`, `com.astrolog.ui.dialog.RegisterDialog`

### 3.7 MainFrame — 接收 User 对象

**文件：** `src/main/java/com/astrolog/ui/frame/MainFrame.java`

**需要修改的位置（按行号顺序）：**

**① 字段声明区 — 替换 currentUsername/currentUserRole：**

```java
// 删除: private String currentUsername; private String currentUserRole;
// 新增:
private User currentUser;
```

**② 构造器 — 修改参数和赋值：**

```java
public MainFrame(User user) {
    this.currentUser = user;
    // 原状态栏 setText(username + " | " + role)
    // 改为：
    statusLabel.setText(user.getUsername() + " | "
        + user.getRole().getDisplayName());
    // ...
}
```

**③ DashboardPanel 初始化 — 传入 user：**

```java
// 原: dashboardPanel = new DashboardPanel();
// 改为:
dashboardPanel = new DashboardPanel(currentUser);
```

**④ 导航"用户中心"按钮 — 传入 user：**

```java
// 在用户中心按钮的 ActionListener 中：
contentPanel.add(new UserPanel(currentUser), "user");
```

**⑤ 导航栏 admin 判断 — 显示/隐藏管理入口：**

```java
// 如果是 admin，显示额外的管理按钮（如星体管理快捷入口）
// 导航按钮面板中，在创建各导航按钮时：
if (currentUser.getRole() == UserRole.ADMIN) {
    // adminBodyButton 可见
}
```

### 3.8 UserPanel — 个人信息与管理面板

**文件：** `src/main/java/com/astrolog/ui/panel/UserPanel.java`

**布局结构（使用 BoxLayout 垂直排列 + 两个区域面板）：**

```
┌──────────────────────────────────────────────┐
│ BorderLayout 或 BoxLayout 垂直排列            │
│                                              │
│ ┌─ 个人信息区域 ──────────────────────────┐  │
│ │ TitledBorder("个人信息")                │  │
│ │                                         │  │
│ │ 用户名:     [JLabel, 只读]             │  │
│ │ 角色:       [JLabel, 只读]             │  │
│ │ 注册时间:   [JLabel, 只读]             │  │
│ │ 最后登录:   [JLabel, 只读]              │  │
│ │ 所在城市:   [JTextField, 可编辑]       │  │
│ │ 默认纬度:   [JTextField, 可编辑]       │  │
│ │ 默认经度:   [JTextField, 可编辑]       │  │
│ │                                         │  │
│ │ [修改密码] [保存修改] [头像(占位)]       │  │
│ └─────────────────────────────────────────┘  │
│                                              │
│ ┌─ 用户管理区域 (仅 admin 可见) ───────────┐  │
│ │ TitledBorder("用户管理")                │  │
│ │                                         │  │
│ │ JTable (5列): 用户名|角色|状态|最后登录│  │
│ │   数据源: DefaultTableModel             │  │
│ │   刷新: listAllUsers() 结果逐行填充       │  │
│ │                                         │  │
│ │ [启用] [禁用] [重置密码]                 │  │
│ └─────────────────────────────────────────┘  │
│                                              │
└──────────────────────────────────────────────┘
```

**实现要点：**

- 构造器签名：`public UserPanel(User currentUser)`
- 个人信息区域中只读字段用 JLabel，可编辑字段用 JTextField 预填充当前值；
- "修改密码"按钮弹出 JDialog：原密码 JPasswordField + 新密码 JPasswordField + 确认密码 JPasswordField + 确认/取消；
- "保存修改"调用 `userService.updateProfile(userId, city, lat, lon)`，成功刷新显示；
- "头像"按钮 S2 仅是一个灰色的 JButton 显示"头像(开发中)"，不实现功能；
- 管理员区域中的 JTable 使用 DefaultTableModel，刷新时清空再逐行添加；
- 状态列从 `lockedUntil` 判断：null 或已过期 → "正常"；远未来日期 → "已禁用"；当前锁定中 → "临时锁定"；
- "启用"调用 `userService.setUserEnabled(selectedUserId, true)`；
- "禁用"调用 `userService.setUserEnabled(selectedUserId, false)`；
- "重置密码"弹出 JOptionPane.showInputDialog 输入新密码，调用 `userService.resetPassword(selectedUserId, newPassword)`；
- 管理员区域通过 `userPanel.setVisible(currentUser.getRole() == UserRole.ADMIN)` 控制可见性。

### 3.9 DashboardPanel — 动态欢迎语

**文件：** `src/main/java/com/astrolog/ui/panel/DashboardPanel.java`

**修改处：**

1. 新增字段 `private User currentUser;`
2. 构造器改为 `public DashboardPanel(User user) { this.currentUser = user; ... }`
3. 欢迎 JLabel 从静态 `"欢迎使用 AstroLog"` 改为 `"欢迎回来，" + user.getUsername()`
4. 如果 `user.getRole() == UserRole.ADMIN`，额外显示一个快捷入口卡片 "星体管理"
5. 底部摘要区三组数字维持占位：`"0 次观测 / 0 个星体 / 0 件器材"`

### 3.10 国际化资源文件

**文件：** `src/main/resources/i18n/messages_zh.properties`

```properties
# 应用
app.title=AstroLog - 天文观测日志与星体管理系统

# 菜单
menu.file=文件
menu.file.exit=退出
menu.data=数据管理
menu.stats=统计分析
menu.help=帮助
menu.help.about=关于

# 导航
nav.dashboard=仪表盘
nav.bodies=星体库
nav.observations=我的观测
nav.equipment=器材柜
nav.sites=观测地
nav.nightsky=今夜星空
nav.stats=统计图表
nav.messier=梅西耶马拉松
nav.constellation=星座文化馆
nav.settings=系统设置
nav.user=用户中心

# 登录
login.title=登录
login.username=用户名
login.password=密码
login.button=登录
login.register=注册
login.emptyFields=请输入用户名和密码
login.success=登录成功
login.fail=用户名或密码错误
login.locked=账号已锁定，请于 %d 分钟后重试

# 注册
register.title=用户注册
register.success=注册成功，请登录
register.fail=注册失败，请重试
register.duplicate=用户名已存在
register.passwordMismatch=两次密码不一致
register.confirm=确认密码
register.city=所在城市
register.lat=默认纬度
register.lon=默认经度
register.role=角色
register.role.observer=观测者
register.role.admin=管理员

# 用户中心
user.profile=个人信息
user.profile.username=用户名
user.profile.role=角色
user.profile.created=注册时间
user.profile.lastLogin=最后登录
user.profile.city=所在城市
user.profile.lat=默认纬度
user.profile.lon=默认经度
user.changePassword=修改密码
user.changePassword.title=修改密码
user.oldPassword=原密码
user.newPassword=新密码
user.confirmPassword=确认新密码
user.passwordMismatch=两次密码不一致
user.wrongOldPassword=原密码错误
user.passwordChanged=密码修改成功
user.saveSuccess=个人信息已更新
user.avatar=头像(开发中)

# 管理员
user.admin.title=用户管理
user.admin.enable=启用
user.admin.disable=禁用
user.admin.resetPassword=重置密码
user.admin.status.active=正常
user.admin.status.locked=临时锁定
user.admin.status.disabled=已禁用

# 校验
validator.username=用户名至少4个字符，最多20个字符，只能包含字母、数字和下划线
validator.password=密码至少8个字符，必须包含大写字母、小写字母和数字
```

**文件：** `src/main/resources/i18n/messages_en.properties`

```properties
# Application
app.title=AstroLog - Astronomical Observation Log & Star Management

# Menu
menu.file=File
menu.file.exit=Exit
menu.data=Data Management
menu.stats=Statistics
menu.help=Help
menu.help.about=About

# Navigation
nav.dashboard=Dashboard
nav.bodies=Celestial Bodies
nav.observations=My Observations
nav.equipment=Equipment
nav.sites=Observation Sites
nav.nightsky=Night Sky
nav.stats=Charts
nav.messier=Messier Marathon
nav.constellation=Constellation Culture
nav.settings=Settings
nav.user=User Center

# Login
login.title=Login
login.username=Username
login.password=Password
login.button=Login
login.register=Register
login.emptyFields=Please enter username and password
login.success=Login successful
login.fail=Incorrect username or password
login.locked=Account locked. Please retry in %d minutes.

# Register
register.title=Register
register.success=Registration successful. Please login.
register.fail=Registration failed. Please retry.
register.duplicate=Username already exists
register.passwordMismatch=Passwords do not match
register.confirm=Confirm Password
register.city=City
register.lat=Default Latitude
register.lon=Default Longitude
register.role=Role
register.role.observer=Observer
register.role.admin=Administrator

# User Center
user.profile=Profile
user.profile.username=Username
user.profile.role=Role
user.profile.created=Registered
user.profile.lastLogin=Last Login
user.profile.city=City
user.profile.lat=Default Latitude
user.profile.lon=Default Longitude
user.changePassword=Change Password
user.changePassword.title=Change Password
user.oldPassword=Current Password
user.newPassword=New Password
user.confirmPassword=Confirm New Password
user.passwordMismatch=Passwords do not match
user.wrongOldPassword=Current password is incorrect
user.passwordChanged=Password changed successfully
user.saveSuccess=Profile updated
user.avatar=Avatar (Coming Soon)

# Admin
user.admin.title=User Management
user.admin.enable=Enable
user.admin.disable=Disable
user.admin.resetPassword=Reset Password
user.admin.status.active=Active
user.admin.status.locked=Temporarily Locked
user.admin.status.disabled=Disabled

# Validation
validator.username=Username must be 4-20 characters with letters, digits, and underscores
validator.password=Password must be at least 8 characters with uppercase, lowercase, and digits
```

**i18n 使用方式（S2 仅 LoginFrame 接入）：**

```java
// 在 LoginFrame 中加载资源束
private static final ResourceBundle i18n =
    ResourceBundle.getBundle("i18n.messages_zh");

// 使用: loginButton.setText(i18n.getString("login.button"));
```

S2 不要求所有界面使用 i18n——可以在 S9 统一改造。但 LoginFrame 的按钮和提示文字从 i18n 读取。

### 3.11 枚举补充 — UserRole.fromString()

S2 需要 `UserRole.fromString()` 方法将数据库值/UI 值转换为枚举。在 `model/enums/UserRole.java` 中添加：

```java
public static UserRole fromString(String s) {
    if (s == null) return OBSERVER;
    for (UserRole r : values()) {
        if (r.name().equalsIgnoreCase(s) || r.displayName.equals(s)) {
            return r;
        }
    }
    return OBSERVER; // 默认
}
```

---

## 4. 测试规格

### 4.1 UserServiceTest（12 例）

**文件：** `src/test/java/com/astrolog/unit/service/UserServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserDao userDao;
    @Mock private LogDao logDao;
    @InjectMocks private UserService userService;

    // UT-US-001
    @Test void register_success() {
        when(userDao.findByUsername("testuser")).thenReturn(null);
        when(userDao.insert(any(User.class))).thenReturn(1);
        ServiceResult r = userService.register("testuser", "Abc12345",
            "observer", "北京", BigDecimal.valueOf(39.9), BigDecimal.valueOf(116.4));
        assertTrue(r.isSuccess());
    }

    // UT-US-002
    @Test void register_duplicateUsername() {
        when(userDao.findByUsername("existing")).thenReturn(new User());
        ServiceResult r = userService.register("existing", "Abc12345",
            "observer", "", null, null);
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("已存在"));
    }

    // UT-US-003
    @Test void register_weakPassword() {
        ServiceResult r = userService.register("testuser", "123",
            "observer", "", null, null);
        assertFalse(r.isSuccess());
    }

    // UT-US-004
    @Test void login_success() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        when(userDao.findByUsername("testuser")).thenReturn(u);
        LoginResult r = userService.login("testuser", "Abc12345");
        assertTrue(r.isSuccess());
        assertEquals("testuser", r.getUser().getUsername());
    }

    // UT-US-005
    @Test void login_wrongPassword() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        when(userDao.findByUsername("testuser")).thenReturn(u);
        LoginResult r = userService.login("testuser", "wrongpassword");
        assertFalse(r.isSuccess());
        verify(userDao).updateLoginAttempts(eq(u.getUserId()), eq(1));
    }

    // UT-US-006
    @Test void login_accountLocked() {
        User u = buildTestUser("lockeduser", PasswordUtil.hash("Abc12345"));
        u.setLockedUntil(LocalDateTime.now().plusMinutes(25));
        when(userDao.findByUsername("lockeduser")).thenReturn(u);
        LoginResult r = userService.login("lockeduser", "Abc12345");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("锁定"));
    }

    // UT-US-007
    @Test void login_incrementAttempts() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        u.setLoginAttempts(0);
        when(userDao.findByUsername("testuser")).thenReturn(u);
        userService.login("testuser", "wrong");
        verify(userDao).updateLoginAttempts(u.getUserId(), 1);
    }

    // UT-US-008
    @Test void login_lockAfter5Failures() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        u.setLoginAttempts(4);
        when(userDao.findByUsername("testuser")).thenReturn(u);
        LoginResult r = userService.login("testuser", "wrong");
        assertFalse(r.isSuccess());
        verify(userDao).updateLoginAttempts(u.getUserId(), 5);
        verify(userDao).updateLockedUntil(eq(u.getUserId()), any(LocalDateTime.class));
    }

    // UT-US-009
    @Test void updateProfile_success() {
        User u = buildTestUser("testuser", "hash");
        when(userDao.findById(1)).thenReturn(u);
        when(userDao.update(any(User.class))).thenReturn(true);
        ServiceResult r = userService.updateProfile(1, "上海",
            BigDecimal.valueOf(31.2), BigDecimal.valueOf(121.5));
        assertTrue(r.isSuccess());
    }

    // UT-US-010
    @Test void changePassword_wrongOldPassword() {
        User u = buildTestUser("testuser", PasswordUtil.hash("OldPass1"));
        when(userDao.findById(1)).thenReturn(u);
        ServiceResult r = userService.changePassword(1, "WrongOld1", "NewPass1");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("原密码错误"));
    }

    // UT-US-011
    @Test void admin_disableUser() {
        ServiceResult r = userService.setUserEnabled(2, false);
        assertTrue(r.isSuccess());
        verify(userDao).updateLockedUntil(eq(2), any(LocalDateTime.class));
    }

    // UT-US-012
    @Test void admin_resetPassword() {
        ServiceResult r = userService.resetPassword(2, "NewPass123");
        assertTrue(r.isSuccess());
        verify(userDao).updatePassword(eq(2), anyString());
    }

    private User buildTestUser(String username, String hashedPassword) {
        User u = new User();
        u.setUserId(1);
        u.setUsername(username);
        u.setPassword(hashedPassword);
        u.setRole(UserRole.OBSERVER);
        u.setLoginAttempts(0);
        return u;
    }
}
```

### 4.2 UserDaoTest（5 例）

**文件：** `src/test/java/com/astrolog/integration/dao/UserDaoTest.java`

```
@BeforeEach: 删除测试数据 (DELETE FROM users WHERE username LIKE 'test_%')
@AfterEach:  删除测试数据

IT-UD-001: insert → findById → 所有字段值一致
IT-UD-002: insert → findByUsername → 精确匹配，大小写敏感
IT-UD-003: insert → 返回自增 ID > 0
IT-UD-004: insert → update city/lat/lon → findById → 更新后字段正确
IT-UD-005: insert → findById 确认存在 → delete → findById 返回 null
```

### 4.3 LogDaoTest（3 例）

**文件：** `src/test/java/com/astrolog/integration/dao/LogDaoTest.java`

```
@BeforeEach: 删除测试日志 (DELETE FROM operation_logs WHERE user_id = -1)
@AfterEach:  删除测试日志

IT-LD-001: insert → findById → 日志内容一致
IT-LD-002: 插入 2 条不同 userId 日志 → findByUserId(userId1) → 只返回 1 条
IT-LD-003: 插入 2 条不同时间日志 → findByTimeRange → 筛选正确
```

---

## 5. 任务执行顺序

按依赖关系排列：

```
第 1 步 → ServiceResult.java + LoginResult.java（无依赖）
第 2 步 → UserDao.java（依赖 BaseDao，S1 已有）
第 3 步 → LogDao.java（依赖 BaseDao，S1 已有）
第 4 步 → UserService.java（依赖以上全部）
第 5 步 → RegisterDialog.java（依赖 UserService）
第 6 步 → 修改 LoginFrame.java（依赖 UserService + RegisterDialog）
第 7 步 → 修改 DashboardPanel.java（依赖 User）
第 8 步 → UserPanel.java（依赖 UserService + 修改后的 MainFrame）
第 9 步 → 修改 MainFrame.java（依赖 UserPanel + DashboardPanel 修改）
第 10 步 → i18n 文件（独立）
第 11 步 → 枚举补充 UserRole.fromString()
第 12 步 → 测试类
```

---

## 6. 验收标准

| #   | 验收项                                                                | 验证方法                             |
| --- | --------------------------------------------------------------------- | ------------------------------------ |
| 1   | `mvn clean compile` 零错误                                            | Maven 编译                           |
| 2   | `mvn test` 27/27 通过（S1 7 + S2 20）                                 | 执行测试                             |
| 3   | 注册新用户成功，重名被拒，弱密码被拒                                  | 手动操作 LoginFrame → RegisterDialog |
| 4   | 正确密码登录 → MainFrame，状态栏显示用户名+角色，Dashboard 显示欢迎语 | 手动操作                             |
| 5   | 错误密码被拒；连续 5 次后账号锁定                                     | 手动操作                             |
| 6   | 登录后 UserPanel 显示个人信息，可修改城市/坐标并保存                  | 手动操作                             |
| 7   | 修改密码：原密码错误被拒 / 正确原密码可改新密码 / 新密码弱拒绝        | 手动操作                             |
| 8   | admin 登录后 UserPanel 显示用户管理表格，可启用/禁用/重置密码         | 手动操作 admin                       |
| 9   | operation_logs 表有注册/登录/管理操作记录                             | `SELECT * FROM operation_logs`       |
| 10  | UI 响应流畅，EDT 无卡顿                                               | 主观体验                             |

---

## 7. 注意事项

1. **BCrypt 性能：** `BCrypt.hashpw()` 生成盐耗时约 100ms，在 EDT 中调用可能引起短暂卡顿。如果感知到延迟，将注册/登录操作包装在 `SwingWorker` 中，操作完成后在 `done()` 回调中更新 UI。
2. **User 对象传递方式：** S2 使用构造器注入（`new MainFrame(user)` → `new DashboardPanel(user)` → `new UserPanel(user)`），不引入全局单例 Session。这是最简单且足够用的方式。
3. **update vs updatePassword vs updateLoginAttempts：** 三个 update 方法各自独立，避免一个方法同时覆盖不相关的字段。`update` 只更新个人信息字段（city/lat/lon/avatar），`updatePassword` 只更新密码，`updateLoginAttempts` / `updateLockedUntil` / `updateLastLogin` 各司其职。
4. **LoginFrame 的 EDT 约定：** AppMain 中通过 `SwingUtilities.invokeLater()` 启动 LoginFrame，S2 不要改变这个调用方式。
5. **集成测试的数据隔离：** 使用前缀 `test_` 区分测试用户（如 `test_user001`），在 @BeforeEach/@AfterEach 中清理。不要影响真实用户数据。
6. **ip_address 字段：** 填 `"127.0.0.1"`（桌面应用无真实外部 IP）。
7. **JaCoCo：** 当前 JaCoCo 0.8.12 不支持 Java 25 class file v69，已从 pom.xml 中移除执行绑定。不要恢复。JaCoCo 报告后续手动执行 `mvn jacoco:report` 即可（等兼容版本发布后）。
8. **UserRole 枚举位置：** S1 创建在 `model/enums/UserRole.java`，S2 只需在其中新增 `fromString()` 方法，不要移动文件。
