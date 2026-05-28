package com.astrolog.model;

import java.time.LocalDateTime;

public class UserFavorite {
    private int userId;
    private int bodyId;
    private LocalDateTime createTime;

    public UserFavorite() {}

    public UserFavorite(int userId, int bodyId, LocalDateTime createTime) {
        this.userId = userId;
        this.bodyId = bodyId;
        this.createTime = createTime;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBodyId() { return bodyId; }
    public void setBodyId(int bodyId) { this.bodyId = bodyId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "UserFavorite{userId=" + userId + ", bodyId=" + bodyId + "}";
    }
}
