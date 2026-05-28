package com.rental.protocol;

import java.io.Serializable;

/**
 * 统一API响应封装类
 * 所有服务端响应都使用此格式
 * 包含成功状态、响应码、消息和数据载荷
 *
 * @author 系统
 * @version 1.0
 * @param <T> 响应数据类型
 */
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private int code;
    private String message;
    private T data;
    private String requestId;

    /**
     * 构造函数
     */
    public ApiResponse() {
    }

    /**
     * 创建成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应实例
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    /**
     * 创建成功响应
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应实例
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 创建错误响应
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应实例
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    /**
     * 获取成功状态
     * @return 成功状态
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置成功状态
     * @param success 成功状态
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取响应码
     * @return 响应码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置响应码
     * @param code 响应码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应数据
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
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
}