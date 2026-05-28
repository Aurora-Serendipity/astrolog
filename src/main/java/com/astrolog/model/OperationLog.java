package com.astrolog.model;

import java.time.LocalDateTime;

public class OperationLog {
    private int logId;
    private int userId;
    private String operation;
    private String detail;
    private String ipAddress;
    private LocalDateTime createTime;

    public OperationLog() {}

    public OperationLog(int logId, int userId, String operation, String detail,
                        String ipAddress, LocalDateTime createTime) {
        this.logId = logId;
        this.userId = userId;
        this.operation = operation;
        this.detail = detail;
        this.ipAddress = ipAddress;
        this.createTime = createTime;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "OperationLog{logId=" + logId + ", userId=" + userId + ", operation='" + operation + "'}";
    }
}
