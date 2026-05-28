package com.rental.protocol;

import java.io.Serializable;
import java.util.UUID;

/**
 * 消息协议类
 * 用于客户端与服务端之间的JSON消息封装
 * 包含消息类型、请求ID、时间戳和数据载荷
 *
 * @author 系统
 * @version 1.0
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private String requestId;
    private long timestamp;
    private Object data;

    /**
     * 构造函数
     * 自动生成请求ID和时间戳
     */
    public Message() {
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造函数
     * @param type 消息类型
     * @param data 数据载荷
     */
    public Message(String type, Object data) {
        this();
        this.type = type;
        this.data = data;
    }

    /**
     * 创建消息实例的静态方法
     * @param type 消息类型
     * @param data 数据载荷
     * @return 消息实例
     */
    public static Message create(String type, Object data) {
        return new Message(type, data);
    }

    /**
     * 获取消息类型
     * @return 消息类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置消息类型
     * @param type 消息类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取请求ID
     * @return 请求ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 设置请求ID
     * @param requestId 请求ID
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取时间戳
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置时间戳
     * @param timestamp 时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取数据载荷
     * @return 数据载荷
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置数据载荷
     * @param data 数据载荷
     */
    public void setData(Object data) {
        this.data = data;
    }
}