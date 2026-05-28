package com.astrolog.service;

import com.astrolog.config.AppConfig;
import com.astrolog.dao.LogDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.OperationLog;
import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import com.astrolog.util.PasswordUtil;
import com.astrolog.util.ValidationResult;
import com.astrolog.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserService {

    private final UserDao userDao;
    private final LogDao logDao;

    public UserService() {
        this(new UserDao(), new LogDao());
    }

    UserService(UserDao userDao, LogDao logDao) {
        this.userDao = userDao;
        this.logDao = logDao;
    }

    // ==================== 注册 ====================

    public ServiceResult register(String username, String password,
                                   String role, String city,
                                   BigDecimal lat, BigDecimal lon) {
        ValidationResult vr = Validator.usernameValidator().validateFirst(username);
        if (!vr.isValid()) return ServiceResult.fail(vr.getMessage());

        vr = Validator.passwordValidator().validateFirst(password);
        if (!vr.isValid()) return ServiceResult.fail(vr.getMessage());

        if (userDao.findByUsername(username) != null) {
            return ServiceResult.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hash(password));
        user.setRole(UserRole.fromString(role));
        user.setCity(city);
        user.setDefaultLat(lat);
        user.setDefaultLon(lon);

        int userId = userDao.insert(user);
        if (userId <= 0) {
            return ServiceResult.fail("注册失败，请重试");
        }

        writeLog(userId, "注册", "新用户注册: " + username);

        return ServiceResult.success("注册成功");
    }

    // ==================== 登录 ====================

    public LoginResult login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return LoginResult.fail("用户名或密码错误");
        }

        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutes = ChronoUnit.MINUTES.between(
                LocalDateTime.now(), user.getLockedUntil());
            return LoginResult.fail(String.format(
                "账号已锁定，请于 %d 分钟后重试", Math.max(1, minutes)));
        }

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
        ValidationResult vr = Validator.passwordValidator().validateFirst(newPassword);
        if (!vr.isValid()) return ServiceResult.fail(vr.getMessage());

        User user = userDao.findById(userId);
        if (user == null) return ServiceResult.fail("用户不存在");
        if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
            return ServiceResult.fail("原密码错误");
        }

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
