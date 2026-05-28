package com.rental.server;

import com.google.gson.Gson;
import com.rental.model.RentalRecord;
import com.rental.model.Vehicle;
import com.rental.protocol.ApiResponse;
import com.rental.protocol.Message;
import com.rental.protocol.MessageType;
import com.rental.service.RentalService;
import com.rental.service.VehicleService;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * 客户端请求处理器
 * 每个客户端连接对应一个ClientHandler实例
 * 在独立线程中运行，处理该客户端的所有请求
 *
 * @author 系统
 * @version 1.0
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Gson gson;
    private final VehicleService vehicleService;
    private final RentalService rentalService;
    private PrintWriter writer;
    private BufferedReader reader;

    /**
     * 构造函数
     * @param socket 客户端Socket
     */
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.gson = new Gson();
        this.vehicleService = new VehicleService();
        this.rentalService = new RentalService();
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String requestJson;
            while ((requestJson = reader.readLine()) != null) {
                Message request = gson.fromJson(requestJson, Message.class);
                Message response = processRequest(request);
                writer.println(gson.toJson(response));
            }
        } catch (IOException e) {
            System.err.println("处理客户端请求异常: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * 处理请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message processRequest(Message request) {
        String type = request.getType();
        try {
            switch (type) {
                case "QUERY_VEHICLES":
                    return handleQueryVehicles(request);
                case "GET_VEHICLE":
                    return handleGetVehicle(request);
                case "ADD_VEHICLE":
                    return handleAddVehicle(request);
                case "UPDATE_VEHICLE":
                    return handleUpdateVehicle(request);
                case "DELETE_VEHICLE":
                    return handleDeleteVehicle(request);
                case "RENT_VEHICLE":
                    return handleRentVehicle(request);
                case "RETURN_VEHICLE":
                    return handleReturnVehicle(request);
                case "QUERY_RENTALS":
                    return handleQueryRentals(request);
                default:
                    return createErrorResponse(request, 400, "未知消息类型: " + type);
            }
        } catch (Exception e) {
            return createErrorResponse(request, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 处理查询车辆请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleQueryVehicles(Message request) {
        Map<String, String> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        java.util.List<Vehicle> vehicles;
        if (data != null && "可租赁".equals(data.get("status"))) {
            vehicles = vehicleService.getAvailableVehicles();
        } else {
            vehicles = vehicleService.getAllVehicles();
        }
        return createSuccessResponse(request, vehicles);
    }

    /**
     * 处理获取单个车辆请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleGetVehicle(Message request) {
        Map<String, String> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        Vehicle vehicle = vehicleService.getVehicleById(data.get("vehicleId"));
        if (vehicle == null) {
            return createErrorResponse(request, 404, "车辆不存在");
        }
        return createSuccessResponse(request, vehicle);
    }

    /**
     * 处理添加车辆请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleAddVehicle(Message request) {
        Vehicle vehicle = gson.fromJson(gson.toJson(request.getData()), Vehicle.class);
        vehicleService.addVehicle(vehicle);
        return createSuccessResponse(request, vehicle);
    }

    /**
     * 处理更新车辆请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleUpdateVehicle(Message request) {
        Vehicle vehicle = gson.fromJson(gson.toJson(request.getData()), Vehicle.class);
        boolean success = vehicleService.updateVehicle(vehicle);
        return success ? createSuccessResponse(request, "车辆更新成功")
                       : createErrorResponse(request, 404, "车辆不存在");
    }

    /**
     * 处理删除车辆请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleDeleteVehicle(Message request) {
        Map<String, String> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        boolean success = vehicleService.deleteVehicle(data.get("vehicleId"));
        return success ? createSuccessResponse(request, "车辆删除成功")
                       : createErrorResponse(request, 404, "车辆不存在");
    }

    /**
     * 处理租车请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleRentVehicle(Message request) {
        Map<String, Object> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        String vehicleId = (String) data.get("vehicleId");
        String customerName = (String) data.get("customerName");
        int days = ((Number) data.get("days")).intValue();

        RentalRecord record = rentalService.rentVehicle(vehicleId, customerName, days);
        if (record == null) {
            return createErrorResponse(request, 400, "租车失败，车辆不可用");
        }
        return createSuccessResponse(request, record);
    }

    /**
     * 处理还车请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleReturnVehicle(Message request) {
        Map<String, String> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        boolean success = rentalService.returnVehicle(data.get("vehicleId"));
        return success ? createSuccessResponse(request, "还车成功")
                       : createErrorResponse(request, 400, "还车失败");
    }

    /**
     * 处理查询租赁记录请求
     * @param request 请求消息
     * @return 响应消息
     */
    private Message handleQueryRentals(Message request) {
        return createSuccessResponse(request, rentalService.getAllRentals());
    }

    /**
     * 创建成功响应
     * @param request 请求消息
     * @param data 响应数据
     * @return 响应消息
     */
    private Message createSuccessResponse(Message request, Object data) {
        ApiResponse<Object> response = ApiResponse.success(data);
        response.setRequestId(request.getRequestId());
        return new Message(MessageType.RESPONSE.getCode(), response);
    }

    /**
     * 创建错误响应
     * @param request 请求消息
     * @param code 错误码
     * @param message 错误消息
     * @return 响应消息
     */
    private Message createErrorResponse(Message request, int code, String message) {
        ApiResponse<Object> response = ApiResponse.error(code, message);
        response.setRequestId(request.getRequestId());
        return new Message(MessageType.RESPONSE.getCode(), response);
    }

    /**
     * 关闭连接
     */
    private void closeConnection() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("关闭连接异常: " + e.getMessage());
        }
    }
}