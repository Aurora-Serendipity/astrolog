package com.rental.protocol;

/**
 * 消息类型枚举
 * 定义客户端与服务端通信的所有消息类型
 *
 * @author 系统
 * @version 1.0
 */
public enum MessageType {
    QUERY_VEHICLES("QUERY_VEHICLES", "查询车辆列表"),
    GET_VEHICLE("GET_VEHICLE", "获取单个车辆"),
    ADD_VEHICLE("ADD_VEHICLE", "添加车辆"),
    UPDATE_VEHICLE("UPDATE_VEHICLE", "更新车辆"),
    DELETE_VEHICLE("DELETE_VEHICLE", "删除车辆"),
    RENT_VEHICLE("RENT_VEHICLE", "租车"),
    RETURN_VEHICLE("RETURN_VEHICLE", "还车"),
    QUERY_RENTALS("QUERY_RENTALS", "查询租赁记录"),
    RESPONSE("RESPONSE", "响应消息");

    private String code;
    private String description;

    MessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}