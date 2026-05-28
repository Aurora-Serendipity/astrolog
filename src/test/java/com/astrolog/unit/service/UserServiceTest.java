package com.astrolog.unit.service;

import com.astrolog.dao.LogDao;
import com.astrolog.dao.UserDao;
import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import com.astrolog.service.LoginResult;
import com.astrolog.service.ServiceResult;
import com.astrolog.service.UserService;
import com.astrolog.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private LogDao logDao;

    @InjectMocks
    private UserService userService;

    // UT-US-001
    @Test
    void register_success() {
        when(userDao.findByUsername("testuser")).thenReturn(null);
        when(userDao.insert(any(User.class))).thenReturn(1);

        ServiceResult r = userService.register("testuser", "Abc12345",
            "observer", "北京", BigDecimal.valueOf(39.9), BigDecimal.valueOf(116.4));
        assertTrue(r.isSuccess());
    }

    // UT-US-002
    @Test
    void register_duplicateUsername() {
        when(userDao.findByUsername("existing")).thenReturn(new User());

        ServiceResult r = userService.register("existing", "Abc12345",
            "observer", "", null, null);
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("已存在"));
    }

    // UT-US-003
    @Test
    void register_weakPassword() {
        ServiceResult r = userService.register("testuser", "123",
            "observer", "", null, null);
        assertFalse(r.isSuccess());
    }

    // UT-US-004
    @Test
    void login_success() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        when(userDao.findByUsername("testuser")).thenReturn(u);

        LoginResult r = userService.login("testuser", "Abc12345");
        assertTrue(r.isSuccess());
        assertEquals("testuser", r.getUser().getUsername());
    }

    // UT-US-005
    @Test
    void login_wrongPassword() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        when(userDao.findByUsername("testuser")).thenReturn(u);

        LoginResult r = userService.login("testuser", "wrongpassword");
        assertFalse(r.isSuccess());
        verify(userDao).updateLoginAttempts(eq(u.getUserId()), eq(1));
    }

    // UT-US-006
    @Test
    void login_accountLocked() {
        User u = buildTestUser("lockeduser", PasswordUtil.hash("Abc12345"));
        u.setLockedUntil(LocalDateTime.now().plusMinutes(25));
        when(userDao.findByUsername("lockeduser")).thenReturn(u);

        LoginResult r = userService.login("lockeduser", "Abc12345");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("锁定"));
    }

    // UT-US-007
    @Test
    void login_incrementAttempts() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        u.setLoginAttempts(0);
        when(userDao.findByUsername("testuser")).thenReturn(u);

        userService.login("testuser", "wrong");
        verify(userDao).updateLoginAttempts(u.getUserId(), 1);
    }

    // UT-US-008
    @Test
    void login_lockAfter5Failures() {
        User u = buildTestUser("testuser", PasswordUtil.hash("Abc12345"));
        u.setLoginAttempts(4);
        when(userDao.findByUsername("testuser")).thenReturn(u);

        LoginResult r = userService.login("testuser", "wrong");
        assertFalse(r.isSuccess());
        verify(userDao).updateLoginAttempts(u.getUserId(), 5);
        verify(userDao).updateLockedUntil(eq(u.getUserId()), any(LocalDateTime.class));
    }

    // UT-US-009
    @Test
    void updateProfile_success() {
        User u = buildTestUser("testuser", "hash");
        when(userDao.findById(1)).thenReturn(u);
        when(userDao.update(any(User.class))).thenReturn(true);

        ServiceResult r = userService.updateProfile(1, "上海",
            BigDecimal.valueOf(31.2), BigDecimal.valueOf(121.5));
        assertTrue(r.isSuccess());
    }

    // UT-US-010
    @Test
    void changePassword_wrongOldPassword() {
        User u = buildTestUser("testuser", PasswordUtil.hash("OldPass1"));
        when(userDao.findById(1)).thenReturn(u);

        ServiceResult r = userService.changePassword(1, "WrongOld1", "NewPass1");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("原密码错误"));
    }

    // UT-US-011
    @Test
    void admin_disableUser() {
        ServiceResult r = userService.setUserEnabled(2, false);
        assertTrue(r.isSuccess());
        verify(userDao).updateLockedUntil(eq(2), any(LocalDateTime.class));
    }

    // UT-US-012
    @Test
    void admin_resetPassword() {
        when(userDao.updatePassword(eq(2), anyString())).thenReturn(true);
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
