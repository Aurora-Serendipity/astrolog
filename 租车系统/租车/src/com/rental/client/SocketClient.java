package com.rental.client;

import com.google.gson.Gson;
import com.rental.protocol.ApiResponse;
import com.rental.protocol.Message;
import com.rental.protocol.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Socket通信客户端
 * 封装与服务端的Socket通信逻辑
 * 提供同步请求-响应模式
 *
 * @author 系统
 * @version 1.0
 */
public class SocketClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    private final Gson gson;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    /**
     * 构造函数
     */
    public SocketClient() {
        this.gson = new Gson();
    }

    /**
     * 连接服务端
     * @return true表示连接成功
     */
    public boolean connect() {
        try {
            socket = new Socket(HOST, PORT);
            socket.setSoTimeout(30000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            System.err.println("连接服务端失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("关闭连接异常: " + e.getMessage());
        }
    }

    /**
     * 发送请求
     * @param type 消息类型
     * @param data 数据
     * @param <T> 响应数据类型
     * @return 响应
     */
    @SuppressWarnings("unchecked")
    public <T> ApiResponse<T> sendRequest(String type, Object data) {
        if (!connect()) {
            ApiResponse<T> error = new ApiResponse<>();
            error.setSuccess(false);
            error.setCode(500);
            error.setMessage("无法连接到服务器");
            return error;
        }

        try {
            Message request = Message.create(type, data);
            writer.println(gson.toJson(request));

            String responseJson = reader.readLine();
            Message response = gson.fromJson(responseJson, Message.class);
            ApiResponse<T> apiResponse = gson.fromJson(gson.toJson(response.getData()), ApiResponse.class);
            return apiResponse;
        } catch (IOException e) {
            ApiResponse<T> error = new ApiResponse<>();
            error.setSuccess(false);
            error.setCode(500);
            error.setMessage("通信异常: " + e.getMessage());
            return error;
        } finally {
            disconnect();
        }
    }

    /**
     * 查询车辆列表
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> queryVehicles() {
        return sendRequest(MessageType.QUERY_VEHICLES.getCode(), null);
    }

    /**
     * 查询可租赁车辆
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> queryAvailableVehicles() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "可租赁");
        return sendRequest(MessageType.QUERY_VEHICLES.getCode(), data);
    }

    /**
     * 获取单个车辆
     * @param vehicleId 车辆ID
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> getVehicle(String vehicleId) {
        Map<String, String> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        return sendRequest(MessageType.GET_VEHICLE.getCode(), data);
    }

    /**
     * 添加车辆
     * @param vehicle 车辆对象
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> addVehicle(Object vehicle) {
        return sendRequest(MessageType.ADD_VEHICLE.getCode(), vehicle);
    }

    /**
     * 更新车辆
     * @param vehicle 车辆对象
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> updateVehicle(Object vehicle) {
        return sendRequest(MessageType.UPDATE_VEHICLE.getCode(), vehicle);
    }

    /**
     * 删除车辆
     * @param vehicleId 车辆ID
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> deleteVehicle(String vehicleId) {
        Map<String, String> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        return sendRequest(MessageType.DELETE_VEHICLE.getCode(), data);
    }

    /**
     * 租车
     * @param vehicleId 车辆ID
     * @param customerName 客户姓名
     * @param days 租赁天数
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> rentVehicle(String vehicleId, String customerName, int days) {
        Map<String, Object> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        data.put("customerName", customerName);
        data.put("days", days);
        return sendRequest(MessageType.RENT_VEHICLE.getCode(), data);
    }

    /**
     * 还车
     * @param vehicleId 车辆ID
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> returnVehicle(String vehicleId) {
        Map<String, String> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        return sendRequest(MessageType.RETURN_VEHICLE.getCode(), data);
    }

    /**
     * 查询租赁记录
     * @param <T> 响应数据类型
     * @return 响应
     */
    public <T> ApiResponse<T> queryRentals() {
        return sendRequest(MessageType.QUERY_RENTALS.getCode(), null);
    }
}