package com.astrolog.service;

import com.astrolog.model.User;

public class LoginResult {
    private final boolean success;
    private final String message;
    private final User user;

    private LoginResult(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public static LoginResult success(User user) {
        return new LoginResult(true, "登录成功", user);
    }

    public static LoginResult fail(String msg) {
        return new LoginResult(false, msg, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
