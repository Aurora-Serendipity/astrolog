package com.astrolog.service;

public class ServiceResult {
    private final boolean success;
    private final String message;

    private ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ServiceResult success(String msg) {
        return new ServiceResult(true, msg);
    }

    public static ServiceResult fail(String msg) {
        return new ServiceResult(false, msg);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
