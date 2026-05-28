# 租车系统重构：前后端分离 + 多线程 + Socket 网络编程

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将当前单机版租车系统重构为前后端分离架构。后端采用 Java Socket + 多线程实现，支持多客户端并发连接，数据存储到 JSON 文件。

**架构：** 客户端与服务端通过 Socket 进行 TCP 通信，采用 JSON 格式传输数据。服务端使用线程池处理多客户端连接，每个客户端请求由独立线程处理。数据持久化采用 JSON 文件存储，支持车辆数据和租赁记录的增删改查。

**技术栈：** Java Socket、Java 多线程（Thread/ExecutorService）、JSON（Gson）、Java Swing、TCP/IP

---

## 一、系统架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端（Client）                         │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   Swing GUI（Java Swing）                 │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐   │    │
│  │  │ 主界面    │  │ 租车对话框 │  │     还车/查询对话框   │   │    │
│  │  └────┬─────┘  └────┬─────┘  └──────────┬───────────┘   │    │
│  │       │             │                   │               │    │
│  │       └─────────────┴───────────────────┘               │    │
│  │                         │                                │    │
│  │                  ┌──────▼──────┐                         │    │
│  │                  │ SocketClient │                         │    │
│  │                  │   Service   │                         │    │
│  │                  └──────┬──────┘                         │    │
│  └─────────────────────────┼─────────────────────────────────┘    │
└────────────────────────────┼─────────────────────────────────────┘
                             │ TCP Socket (JSON协议)
                             │ 连接地址: localhost:8888
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         服务端（Server）                         │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    ServerSocket                         │    │
│  │                      端口: 8888                         │    │
│  │                         │                               │    │
│  │                  ┌──────▼──────┐                         │    │
│  │                  │  线程池管理   │                         │    │
│  │                  │ (ExecutorService)│                     │
│  │                  └──────┬──────┘                         │    │
│  │                         │                                │    │
│  │       ┌─────────────────┼─────────────────┐              │    │
│  │       ▼                 ▼                 ▼              │    │
│  │  ┌─────────┐      ┌─────────┐      ┌─────────┐          │    │
│  │  │线程1    │      │线程2     │      │线程3     │          │    │
│  │  │Client   │      │Client    │      │Client    │          │    │
│  │  │Handler  │      │Handler   │      │Handler   │          │    │
│  │  └────┬────┘      └────┬────┘      └────┬────┘          │    │
│  │       │                │                │               │    │
│  │       └────────────────┴────────────────┘               │    │
│  │                         │                                │    │
│  │                  ┌──────▼──────┐                         │    │
│  │                  │  业务处理器   │                         │    │
│  │                  │(VehicleManager)│                      │
│  │                  └──────┬──────┘                         │    │
│  │                         │                                │    │
│  │                  ┌──────▼──────┐                         │    │
│  │                  │ 文件存储层   │                         │    │
│  │                  │(JSON Files) │                         │    │
│  │                  └─────────────┘                         │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   data/        │
                    │  vehicles.json │
                    │  rentals.json  │
                    └─────────────────┘
```

---

## 二、项目文件结构

```
zuche/
├── src/com/rental/
│   ├── model/                      # 数据模型层
│   │   ├── Vehicle.java             # 车辆模型（已有）
│   │   ├── RentalRecord.java        # 租赁记录模型（新建）
│   │   └── ApiResponse.java         # 统一响应封装（新建）
│   │
│   ├── protocol/                    # 通信协议层
│   │   ├── Message.java             # 消息协议类（新建）
│   │   ├── MessageType.java         # 消息类型枚举（新建）
│   │   └── RequestHandler.java      # 请求处理器接口（新建）
│   │
│   ├── server/                      # 服务端
│   │   ├── ServerApp.java           # 服务端主程序（新建）
│   │   ├── ClientHandler.java       # 客户端请求处理器（新建）
│   │   └── ThreadPoolManager.java   # 线程池管理器（新建）
│   │
│   ├── service/                     # 业务逻辑层
│   │   ├── VehicleService.java      # 车辆业务服务（新建）
│   │   ├── RentalService.java       # 租赁业务服务（新建）
│   │   └── RentalCalculator.java     # 租金计算（已有，保留）
│   │
│   ├── repository/                  # 数据持久化层
│   │   ├── JsonFileStorage.java     # JSON文件存储（新建）
│   │   ├── VehicleRepository.java   # 车辆数据仓库（重构）
│   │   └── RentalRepository.java    # 租赁记录仓库（新建）
│   │
│   └── client/                      # 客户端
│       ├── SocketClient.java        # Socket通信客户端（新建）
│       └── ui/                      # 客户端界面
│           ├── ClientUI.java         # 客户端UI入口（新建）
│           └── ClientMainFrame.java  # 客户端主窗口（重构）
│
├── data/                           # 数据存储目录
│   ├── vehicles.json               # 车辆数据文件
│   └── rentals.json               # 租赁记录文件
│
└── docs/                           # 文档目录
    └── superpowers/
        └── plans/                  # 计划文档
```

---

## 三、通信协议设计

### 3.1 消息格式（JSON）

```json
{
  "type": "QUERY_VEHICLES",
  "requestId": "uuid-xxx",
  "timestamp": 1713000000000,
  "data": {
    "status": "可租赁"
  }
}
```

### 3.2 消息类型枚举

| 类型 | 说明 | 客户端请求数据 | 服务端响应数据 |
|------|------|----------------|----------------|
| QUERY_VEHICLES | 查询车辆列表 | {status?} | List<Vehicle> |
| GET_VEHICLE | 获取单个车辆 | {vehicleId} | Vehicle |
| RENT_VEHICLE | 租车 | {vehicleId, days} | RentalRecord |
| RETURN_VEHICLE | 还车 | {vehicleId} | {success, message} |
| ADD_VEHICLE | 添加车辆 | Vehicle对象 | {success, vehicleId} |
| UPDATE_VEHICLE | 更新车辆 | Vehicle对象 | {success} |
| DELETE_VEHICLE | 删除车辆 | {vehicleId} | {success} |
| QUERY_RENTALS | 查询租赁记录 | {vehicleId?} | List<RentalRecord> |

### 3.3 统一响应格式

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {},
  "requestId": "uuid-xxx"
}
```

---

## 四、核心类设计

### 4.1 Message.java - 消息协议类

```java
package com.rental.protocol;

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

    private String type;       // 消息类型
    private String requestId;  // 请求唯一标识
    private long timestamp;    // 时间戳
    private Object data;       // 数据载荷

    // 构造函数、getter、setter
}
```

### 4.2 ClientHandler.java - 客户端处理器

```java
package com.rental.server;

/**
 * 客户端请求处理器
 * 每个客户端连接对应一个ClientHandler实例
 * 在独立线程中运行，处理该客户端的所有请求
 *
 * @author 系统
 * @version 1.0
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private VehicleService vehicleService;
    private RentalService rentalService;

    @Override
    public void run() {
        // 1. 从输入流读取消息
        // 2. 解析消息类型
        // 3. 调用对应服务处理
        // 4. 写入响应到输出流
    }
}
```

### 4.3 ServerApp.java - 服务端主程序

```java
package com.rental.server;

/**
 * 服务端主程序
 * 启动ServerSocket，监听客户端连接
 * 使用线程池处理并发请求
 *
 * @author 系统
 * @version 1.0
 */
public class ServerApp {
    private static final int PORT = 8888;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        // 1. 创建线程池
        // 2. 创建ServerSocket
        // 3. 循环接受客户端连接
        // 4. 为每个客户端分配线程处理
    }
}
```

### 4.4 SocketClient.java - Socket客户端

```java
package com.rental.client;

/**
 * Socket通信客户端
 * 封装与服务端的Socket通信逻辑
 * 提供同步请求-响应模式
 *
 * @author 系统
 * @version 1.0
 */
public class SocketClient {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    // 同步发送请求并获取响应
    public Message sendRequest(Message request) {
        // 1. 连接服务端
        // 2. 发送请求
        // 3. 接收响应
        // 4. 关闭连接
    }
}
```

---

## 五、文件存储设计

### 5.1 vehicles.json 结构

```json
{
  "vehicles": [
    {
      "id": "V001",
      "type": "轿车",
      "model": "丰田凯美瑞",
      "dailyRent": 300.0,
      "status": "可租赁"
    }
  ],
  "lastUpdate": "2026-04-22T10:00:00"
}
```

### 5.2 rentals.json 结构

```json
{
  "rentals": [
    {
      "id": "R001",
      "vehicleId": "V001",
      "vehicleModel": "丰田凯美瑞",
      "customerName": "张三",
      "dailyRent": 300.0,
      "days": 3,
      "totalFee": 855,
      "rentalDate": "2026-04-20T10:00:00",
      "returnDate": null,
      "status": "租用中"
    }
  ],
  "lastUpdate": "2026-04-22T10:00:00"
}
```

---

## 六、任务分解

### 任务 1：创建通信协议层

**文件：**
- 创建：`src/com/rental/protocol/MessageType.java`
- 创建：`src/com/rental/protocol/Message.java`
- 创建：`src/com/rental/protocol/ApiResponse.java`

- [ ] **步骤 1：创建 MessageType.java 消息类型枚举**

```java
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
```

- [ ] **步骤 2：创建 Message.java 消息协议类**

```java
package com.rental.protocol;

import java.io.Serializable;
import java.util.UUID;

/**
 * 消息协议类
 * 用于客户端与服务端之间的JSON消息封装
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

    public Message() {
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String type, Object data) {
        this();
        this.type = type;
        this.data = data;
    }

    public static Message create(String type, Object data) {
        return new Message(type, data);
    }

    // getter和setter方法...
}
```

- [ ] **步骤 3：创建 ApiResponse.java 统一响应封装**

```java
package com.rental.protocol;

/**
 * 统一API响应封装类
 *
 * @author 系统
 * @version 1.0
 */
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private int code;
    private String message;
    private T data;
    private String requestId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    // getter和setter方法...
}
```

- [ ] **步骤 4：Commit**

```bash
git add src/com/rental/protocol/
git commit -m "feat: 创建通信协议层基础类"
```

---

### 任务 2：重构数据持久化层（JSON存储）

**文件：**
- 创建：`src/com/rental/repository/JsonFileStorage.java`
- 修改：`src/com/rental/repository/VehicleRepository.java`
- 创建：`src/com/rental/repository/RentalRepository.java`
- 创建：`src/com/rental/model/RentalRecord.java`

- [ ] **步骤 1：创建 RentalRecord.java 租赁记录模型**

```java
package com.rental.model;

import java.io.Serializable;

/**
 * 租赁记录数据模型类
 *
 * @author 系统
 * @version 1.0
 */
public class RentalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String vehicleId;
    private String vehicleModel;
    private String vehicleType;
    private String customerName;
    private double dailyRent;
    private int days;
    private int totalFee;
    private long rentalDate;
    private Long returnDate;
    private String status;

    // getter和setter方法...
}
```

- [ ] **步骤 2：创建 JsonFileStorage.java JSON文件存储工具类**

```java
package com.rental.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON文件存储工具类
 *
 * @author 系统
 * @version 1.0
 */
public class JsonFileStorage<T> {
    private static final String DATA_DIR = "data";
    private final Gson gson;
    private final Type typeOfList;
    private final String fileName;

    public JsonFileStorage(TypeToken<List<T>> typeToken, String fileName) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.typeOfList = typeToken.getType();
        this.fileName = fileName;
        ensureDataDirExists();
    }

    private void ensureDataDirExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void saveList(List<T> list) {
        try (Writer writer = new FileWriter(getFilePath())) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    public List<T> loadList() {
        File file = new File(getFilePath());
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            List<T> list = gson.fromJson(reader, typeOfList);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("加载数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getFilePath() {
        return DATA_DIR + File.separator + fileName;
    }
}
```

- [ ] **步骤 3：重构 VehicleRepository.java 使用JSON存储**

```java
package com.rental.repository;

import com.google.gson.reflect.TypeToken;
import com.rental.model.Vehicle;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 车辆数据仓库
 *
 * @author 系统
 * @version 2.0
 */
public class VehicleRepository {
    private static final String VEHICLES_FILE = "vehicles.json";
    private final JsonFileStorage<Vehicle> storage;

    public VehicleRepository() {
        this.storage = new JsonFileStorage<>(new TypeToken<List<Vehicle>>(){}, VEHICLES_FILE);
    }

    public List<Vehicle> findAll() {
        return storage.loadList();
    }

    public Vehicle findById(String id) {
        for (Vehicle vehicle : storage.loadList()) {
            if (vehicle.getId().equals(id)) {
                return vehicle;
            }
        }
        return null;
    }

    public List<Vehicle> findByStatus(String status) {
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle vehicle : storage.loadList()) {
            if (vehicle.getStatus().equals(status)) {
                result.add(vehicle);
            }
        }
        return result;
    }

    public void save(Vehicle vehicle) {
        List<Vehicle> vehicles = storage.loadList();
        vehicles.add(vehicle);
        storage.saveList(vehicles);
    }

    public void update(Vehicle vehicle) {
        List<Vehicle> vehicles = storage.loadList();
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId().equals(vehicle.getId())) {
                vehicles.set(i, vehicle);
                break;
            }
        }
        storage.saveList(vehicles);
    }

    public void delete(String id) {
        List<Vehicle> vehicles = storage.loadList();
        vehicles.removeIf(v -> v.getId().equals(id));
        storage.saveList(vehicles);
    }

    public void saveAll(List<Vehicle> vehicles) {
        storage.saveList(vehicles);
    }
}
```

- [ ] **步骤 4：创建 RentalRepository.java 租赁记录仓库**

```java
package com.rental.repository;

import com.google.gson.reflect.TypeToken;
import com.rental.model.RentalRecord;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 租赁记录数据仓库
 *
 * @author 系统
 * @version 1.0
 */
public class RentalRepository {
    private static final String RENTALS_FILE = "rentals.json";
    private final JsonFileStorage<RentalRecord> storage;

    public RentalRepository() {
        this.storage = new JsonFileStorage<>(new TypeToken<List<RentalRecord>>(){}, RENTALS_FILE);
    }

    public List<RentalRecord> findAll() {
        return storage.loadList();
    }

    public List<RentalRecord> findByVehicleId(String vehicleId) {
        List<RentalRecord> result = new ArrayList<>();
        for (RentalRecord record : storage.loadList()) {
            if (record.getVehicleId().equals(vehicleId)) {
                result.add(record);
            }
        }
        return result;
    }

    public RentalRecord findActiveRental(String vehicleId) {
        for (RentalRecord record : storage.loadList()) {
            if (record.getVehicleId().equals(vehicleId) && "租用中".equals(record.getStatus())) {
                return record;
            }
        }
        return null;
    }

    public void save(RentalRecord record) {
        List<RentalRecord> records = storage.loadList();
        records.add(record);
        storage.saveList(records);
    }

    public void update(RentalRecord record) {
        List<RentalRecord> records = storage.loadList();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId().equals(record.getId())) {
                records.set(i, record);
                break;
            }
        }
        storage.saveList(records);
    }
}
```

- [ ] **步骤 5：Commit**

```bash
git add src/com/rental/repository/ src/com/rental/model/RentalRecord.java
git commit -m "refactor: 重构数据持久化层为JSON存储"
```

---

### 任务 3：创建业务服务层

**文件：**
- 创建：`src/com/rental/service/VehicleService.java`
- 创建：`src/com/rental/service/RentalService.java`

- [ ] **步骤 1：创建 VehicleService.java 车辆业务服务**

```java
package com.rental.service;

import com.rental.model.Vehicle;
import com.rental.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;

/**
 * 车辆业务服务类
 *
 * @author 系统
 * @version 1.0
 */
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService() {
        this.vehicleRepository = new VehicleRepository();
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByStatus("可租赁");
    }

    public Vehicle getVehicleById(String id) {
        return vehicleRepository.findById(id);
    }

    public boolean addVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null || vehicle.getId().isEmpty()) {
            vehicle.setId("V" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        vehicleRepository.save(vehicle);
        return true;
    }

    public boolean updateVehicle(Vehicle vehicle) {
        Vehicle existing = vehicleRepository.findById(vehicle.getId());
        if (existing == null) {
            return false;
        }
        vehicleRepository.update(vehicle);
        return true;
    }

    public boolean updateVehicleStatus(String vehicleId, String status) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) {
            return false;
        }
        vehicle.setStatus(status);
        vehicleRepository.update(vehicle);
        return true;
    }

    public boolean deleteVehicle(String id) {
        Vehicle vehicle = vehicleRepository.findById(id);
        if (vehicle == null) {
            return false;
        }
        vehicleRepository.delete(id);
        return true;
    }

    public void initializeDefaultData() {
        if (vehicleRepository.findAll().isEmpty()) {
            vehicleRepository.saveAll(createDefaultVehicles());
        }
    }

    private List<Vehicle> createDefaultVehicles() {
        return List.of(
            new Vehicle("V001", "轿车", "丰田凯美瑞", 300.0, "可租赁"),
            new Vehicle("V002", "轿车", "大众帕萨特", 280.0, "可租赁"),
            new Vehicle("V003", "轿车", "本田雅阁", 320.0, "可租赁"),
            new Vehicle("V004", "客车", "宇通大巴", 800.0, "可租赁"),
            new Vehicle("V005", "客车", "金龙客车", 750.0, "可租赁"),
            new Vehicle("V006", "卡车", "东风天龙", 1200.0, "可租赁"),
            new Vehicle("V007", "卡车", "解放J6", 1000.0, "可租赁")
        );
    }
}
```

- [ ] **步骤 2：创建 RentalService.java 租赁业务服务**

```java
package com.rental.service;

import com.rental.model.RentalRecord;
import com.rental.model.Vehicle;
import com.rental.repository.RentalRepository;
import com.rental.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;

/**
 * 租赁业务服务类
 *
 * @author 系统
 * @version 1.0
 */
public class RentalService {
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;

    public RentalService() {
        this.rentalRepository = new RentalRepository();
        this.vehicleRepository = new VehicleRepository();
    }

    public RentalRecord rentVehicle(String vehicleId, String customerName, int days) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null || !"可租赁".equals(vehicle.getStatus())) {
            return null;
        }

        int totalFee = RentalCalculator.calculateRentalFee(vehicle.getType(), vehicle.getDailyRent(), days);

        RentalRecord record = new RentalRecord();
        record.setId("R" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        record.setVehicleId(vehicleId);
        record.setVehicleModel(vehicle.getModel());
        record.setVehicleType(vehicle.getType());
        record.setCustomerName(customerName);
        record.setDailyRent(vehicle.getDailyRent());
        record.setDays(days);
        record.setTotalFee(totalFee);
        record.setRentalDate(System.currentTimeMillis());
        record.setStatus("租用中");

        rentalRepository.save(record);

        vehicle.setStatus("已租赁");
        vehicleRepository.update(vehicle);

        return record;
    }

    public boolean returnVehicle(String vehicleId) {
        RentalRecord record = rentalRepository.findActiveRental(vehicleId);
        if (record == null) {
            return false;
        }

        record.setReturnDate(System.currentTimeMillis());
        record.setStatus("已归还");
        rentalRepository.update(record);

        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle != null) {
            vehicle.setStatus("可租赁");
            vehicleRepository.update(vehicle);
        }

        return true;
    }

    public RentalRecord getActiveRental(String vehicleId) {
        return rentalRepository.findActiveRental(vehicleId);
    }

    public List<RentalRecord> getAllRentals() {
        return rentalRepository.findAll();
    }

    public List<RentalRecord> getRentalsByVehicle(String vehicleId) {
        return rentalRepository.findByVehicleId(vehicleId);
    }
}
```

- [ ] **步骤 3：Commit**

```bash
git add src/com/rental/service/VehicleService.java src/com/rental/service/RentalService.java
git commit -m "feat: 创建业务服务层"
```

---

### 任务 4：创建服务端（多线程 + Socket）

**文件：**
- 创建：`src/com/rental/server/ServerApp.java`
- 创建：`src/com/rental/server/ClientHandler.java`
- 创建：`src/com/rental/server/ThreadPoolManager.java`

- [ ] **步骤 1：创建 ThreadPoolManager.java 线程池管理器**

```java
package com.rental.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池管理器
 * 负责管理服务端处理客户端请求的线程池
 *
 * @author 系统
 * @version 1.0
 */
public class ThreadPoolManager {
    private static final int THREAD_POOL_SIZE = 10;
    private final ExecutorService executorService;

    public ThreadPoolManager() {
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void execute(Runnable task) {
        executorService.execute(task);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
```

- [ ] **步骤 2：创建 ClientHandler.java 客户端请求处理器**

```java
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

    private Message handleGetVehicle(Message request) {
        Map<String, String> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        Vehicle vehicle = vehicleService.getVehicleById(data.get("vehicleId"));
        if (vehicle == null) {
            return createErrorResponse(request, 404, "车辆不存在");
        }
        return createSuccessResponse(request, vehicle);
    }

    private Message handleAddVehicle(Message request) {
        Vehicle vehicle = gson.fromJson(gson.toJson(request.getData()), Vehicle.class);
        vehicleService.addVehicle(vehicle);
        return createSuccessResponse(request, vehicle);
    }

    private Message handleUpdateVehicle(Message request) {
        Vehicle vehicle = gson.fromJson(gson.toJson(request.getData()), Vehicle.class);
        boolean success = vehicleService.updateVehicle(vehicle);
        return success ? createSuccessResponse(request, "车辆更新成功")
                       : createErrorResponse(request, 404, "车辆不存在");
    }

    private Message handleDeleteVehicle(Message request) {
        Map<String, String> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        boolean success = vehicleService.deleteVehicle(data.get("vehicleId"));
        return success ? createSuccessResponse(request, "车辆删除成功")
                       : createErrorResponse(request, 404, "车辆不存在");
    }

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

    private Message handleReturnVehicle(Message request) {
        Map<String, String> data = gson.fromJson(gson.toJson(request.getData()), Map.class);
        boolean success = rentalService.returnVehicle(data.get("vehicleId"));
        return success ? createSuccessResponse(request, "还车成功")
                       : createErrorResponse(request, 400, "还车失败");
    }

    private Message handleQueryRentals(Message request) {
        return createSuccessResponse(request, rentalService.getAllRentals());
    }

    private Message createSuccessResponse(Message request, Object data) {
        ApiResponse<Object> response = ApiResponse.success(data);
        response.setRequestId(request.getRequestId());
        return new Message(MessageType.RESPONSE.getCode(), response);
    }

    private Message createErrorResponse(Message request, int code, String message) {
        ApiResponse<Object> response = ApiResponse.error(code, message);
        response.setRequestId(request.getRequestId());
        return new Message(MessageType.RESPONSE.getCode(), response);
    }

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
```

- [ ] **步骤 3：创建 ServerApp.java 服务端主程序**

```java
package com.rental.server;

import com.rental.service.VehicleService;

/**
 * 服务端主程序
 * 启动ServerSocket，监听客户端连接
 * 使用线程池处理并发请求
 *
 * @author 系统
 * @version 1.0
 */
public class ServerApp {
    private static final int PORT = 8888;
    private static final String SERVER_NAME = "租车系统服务端";

    public static void main(String[] args) {
        System.out.println("========== " + SERVER_NAME + " 启动中 ==========");

        ThreadPoolManager threadPool = new ThreadPoolManager();
        VehicleService vehicleService = new VehicleService();

        vehicleService.initializeDefaultData();
        System.out.println("车辆数据初始化完成");

        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(PORT)) {
            System.out.println("服务端启动成功，监听端口: " + PORT);
            System.out.println("等待客户端连接...");

            while (true) {
                java.net.Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("客户端连接: " + clientAddress);

                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }
        } catch (java.io.IOException e) {
            System.err.println("服务端异常: " + e.getMessage());
        } finally {
            threadPool.shutdown();
            System.out.println("服务端已关闭");
        }
    }
}
```

- [ ] **步骤 4：Commit**

```bash
git add src/com/rental/server/
git commit -m "feat: 创建服务端（多线程+Socket）"
```

---

### 任务 5：创建客户端（Socket通信）

**文件：**
- 创建：`src/com/rental/client/SocketClient.java`

- [ ] **步骤 1：创建 SocketClient.java Socket通信客户端**

```java
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

    public SocketClient() {
        this.gson = new Gson();
    }

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

    public void disconnect() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("关闭连接异常: " + e.getMessage());
        }
    }

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

    public <T> ApiResponse<T> queryVehicles() {
        return sendRequest(MessageType.QUERY_VEHICLES.getCode(), null);
    }

    public <T> ApiResponse<T> getVehicle(String vehicleId) {
        Map<String, String> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        return sendRequest(MessageType.GET_VEHICLE.getCode(), data);
    }

    public <T> ApiResponse<T> addVehicle(Object vehicle) {
        return sendRequest(MessageType.ADD_VEHICLE.getCode(), vehicle);
    }

    public <T> ApiResponse<T> updateVehicle(Object vehicle) {
        return sendRequest(MessageType.UPDATE_VEHICLE.getCode(), vehicle);
    }

    public <T> ApiResponse<T> deleteVehicle(String vehicleId) {
        Map<String, String> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        return sendRequest(MessageType.DELETE_VEHICLE.getCode(), data);
    }

    public <T> ApiResponse<T> rentVehicle(String vehicleId, String customerName, int days) {
        Map<String, Object> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        data.put("customerName", customerName);
        data.put("days", days);
        return sendRequest(MessageType.RENT_VEHICLE.getCode(), data);
    }

    public <T> ApiResponse<T> returnVehicle(String vehicleId) {
        Map<String, String> data = new HashMap<>();
        data.put("vehicleId", vehicleId);
        return sendRequest(MessageType.RETURN_VEHICLE.getCode(), data);
    }

    public <T> ApiResponse<T> queryRentals() {
        return sendRequest(MessageType.QUERY_RENTALS.getCode(), null);
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add src/com/rental/client/SocketClient.java
git commit -m "feat: 创建Socket通信客户端"
```

---

### 任务 6：重构客户端UI

**文件：**
- 创建：`src/com/rental/client/ui/ClientUI.java`
- 创建：`src/com/rental/client/ui/ClientMainFrame.java`

- [ ] **步骤 1：创建 ClientUI.java 客户端UI入口**

```java
package com.rental.client.ui;

import javax.swing.*;

/**
 * 客户端UI入口类
 *
 * @author 系统
 * @version 1.0
 */
public class ClientUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ClientMainFrame mainFrame = new ClientMainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}
```

- [ ] **步骤 2：创建 ClientMainFrame.java 客户端主窗口**

```java
package com.rental.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rental.client.SocketClient;
import com.rental.model.RentalRecord;
import com.rental.model.Vehicle;
import com.rental.protocol.ApiResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 客户端主窗口类
 *
 * @author 系统
 * @version 1.0
 */
public class ClientMainFrame extends JFrame {
    private static final String[] VEHICLE_COLUMNS = {"ID", "类型", "车型", "日租金", "状态"};
    private static final String[] RENTAL_COLUMNS = {"记录ID", "车辆ID", "车型", "客户", "租金/天", "天数", "总费用", "状态"};

    private SocketClient socketClient;
    private JTable vehicleTable;
    private JTable rentalTable;
    private DefaultTableModel vehicleTableModel;
    private DefaultTableModel rentalTableModel;
    private JLabel connectionStatusLabel;

    public ClientMainFrame() {
        socketClient = new SocketClient();
        setTitle("租车系统 - 客户端");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        checkConnectionAndLoadData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionStatusLabel = new JLabel("连接状态: 检测中...");
        JButton refreshButton = new JButton("刷新数据");
        refreshButton.addActionListener(e -> checkConnectionAndLoadData());
        topPanel.add(connectionStatusLabel);
        topPanel.add(refreshButton);

        JTabbedPane tabbedPane = new JTabbedPane();

        vehicleTableModel = new DefaultTableModel(VEHICLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vehicleTable = new JTable(vehicleTableModel);
        JScrollPane vehicleScrollPane = new JScrollPane(vehicleTable);

        JPanel vehicleButtonPanel = new JPanel();
        JButton rentButton = new JButton("租车");
        JButton viewDetailsButton = new JButton("查看详情");
        rentButton.addActionListener(e -> handleRentVehicle());
        viewDetailsButton.addActionListener(e -> handleViewDetails());
        vehicleButtonPanel.add(rentButton);
        vehicleButtonPanel.add(viewDetailsButton);

        JPanel vehiclePanel = new JPanel(new BorderLayout());
        vehiclePanel.add(vehicleScrollPane, BorderLayout.CENTER);
        vehiclePanel.add(vehicleButtonPanel, BorderLayout.SOUTH);

        rentalTableModel = new DefaultTableModel(RENTAL_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rentalTable = new JTable(rentalTableModel);
        JScrollPane rentalScrollPane = new JScrollPane(rentalTable);

        JPanel rentalButtonPanel = new JPanel();
        JButton returnButton = new JButton("还车");
        JButton refreshRentalButton = new JButton("刷新记录");
        returnButton.addActionListener(e -> handleReturnVehicle());
        refreshRentalButton.addActionListener(e -> loadRentalData());
        rentalButtonPanel.add(returnButton);
        rentalButtonPanel.add(refreshRentalButton);

        JPanel rentalPanel = new JPanel(new BorderLayout());
        rentalPanel.add(rentalScrollPane, BorderLayout.CENTER);
        rentalPanel.add(rentalButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("车辆列表", vehiclePanel);
        tabbedPane.addTab("租赁记录", rentalPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void checkConnectionAndLoadData() {
        if (socketClient.connect()) {
            connectionStatusLabel.setText("连接状态: 已连接");
            connectionStatusLabel.setForeground(Color.GREEN);
            socketClient.disconnect();
            loadVehicleData();
            loadRentalData();
        } else {
            connectionStatusLabel.setText("连接状态: 未连接");
            connectionStatusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "无法连接到服务器，请确保服务器已启动", "连接失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadVehicleData() {
        ApiResponse<List<Vehicle>> response = socketClient.queryVehicles();
        vehicleTableModel.setRowCount(0);

        if (response.isSuccess() && response.getData() != null) {
            Gson gson = new Gson();
            List<Vehicle> vehicles = gson.fromJson(gson.toJson(response.getData()),
                    new TypeToken<List<Vehicle>>(){}.getType());
            for (Vehicle v : vehicles) {
                vehicleTableModel.addRow(new Object[]{v.getId(), v.getType(), v.getModel(), v.getDailyRent(), v.getStatus()});
            }
        } else {
            JOptionPane.showMessageDialog(this, "加载车辆数据失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadRentalData() {
        ApiResponse<List<RentalRecord>> response = socketClient.queryRentals();
        rentalTableModel.setRowCount(0);

        if (response.isSuccess() && response.getData() != null) {
            Gson gson = new Gson();
            List<RentalRecord> rentals = gson.fromJson(gson.toJson(response.getData()),
                    new TypeToken<List<RentalRecord>>(){}.getType());
            for (RentalRecord r : rentals) {
                rentalTableModel.addRow(new Object[]{r.getId(), r.getVehicleId(), r.getVehicleModel(),
                        r.getCustomerName(), r.getDailyRent(), r.getDays(), r.getTotalFee(), r.getStatus()});
            }
        }
    }

    private void handleRentVehicle() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一辆车辆");
            return;
        }

        String vehicleId = (String) vehicleTableModel.getValueAt(selectedRow, 0);
        String status = (String) vehicleTableModel.getValueAt(selectedRow, 4);

        if (!"可租赁".equals(status)) {
            JOptionPane.showMessageDialog(this, "该车辆不可租赁");
            return;
        }

        String customerName = JOptionPane.showInputDialog(this, "请输入客户姓名:", "租车信息", JOptionPane.QUESTION_MESSAGE);
        if (customerName == null || customerName.trim().isEmpty()) {
            return;
        }

        String daysStr = JOptionPane.showInputDialog(this, "请输入租赁天数:", "租车信息", JOptionPane.QUESTION_MESSAGE);
        if (daysStr == null || daysStr.trim().isEmpty()) {
            return;
        }

        try {
            int days = Integer.parseInt(daysStr);
            if (days <= 0) {
                JOptionPane.showMessageDialog(this, "天数必须大于0");
                return;
            }

            ApiResponse<RentalRecord> response = socketClient.rentVehicle(vehicleId, customerName.trim(), days);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "租车成功！总费用: " + response.getData().getTotalFee());
                loadVehicleData();
                loadRentalData();
            } else {
                JOptionPane.showMessageDialog(this, "租车失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "天数输入无效");
        }
    }

    private void handleReturnVehicle() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条租赁记录");
            return;
        }

        String status = (String) rentalTableModel.getValueAt(selectedRow, 7);
        if (!"租用中".equals(status)) {
            JOptionPane.showMessageDialog(this, "该车辆未在租赁中");
            return;
        }

        String vehicleId = (String) rentalTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "确认归还车辆 " + vehicleId + " 吗？", "还车确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ApiResponse<Object> response = socketClient.returnVehicle(vehicleId);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "还车成功！");
                loadVehicleData();
                loadRentalData();
            } else {
                JOptionPane.showMessageDialog(this, "还车失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewDetails() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一辆车辆");
            return;
        }

        String vehicleId = (String) vehicleTableModel.getValueAt(selectedRow, 0);
        String vehicleModel = (String) vehicleTableModel.getValueAt(selectedRow, 2);
        double dailyRent = (double) vehicleTableModel.getValueAt(selectedRow, 3);

        String info = "车辆ID: " + vehicleId + "\n" +
                      "车型: " + vehicleModel + "\n" +
                      "日租金: ¥" + dailyRent;

        JOptionPane.showMessageDialog(this, info, "车辆详情", JOptionPane.INFORMATION_MESSAGE);
    }
}
```

- [ ] **步骤 3：Commit**

```bash
git add src/com/rental/client/
git commit -m "refactor: 重构客户端UI为前后端分离架构"
```

---

### 任务 7：添加 Gson 依赖并测试编译

**文件：**
- 创建：`pom.xml`（Maven配置）

- [ ] **步骤 1：创建 pom.xml（Maven配置）**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.rental</groupId>
    <artifactId>zuche</artifactId>
    <version>2.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **步骤 2：编译测试**

```bash
# 使用Maven编译
mvn compile

# 启动服务端
mvn exec:java -Dexec.mainClass="com.rental.server.ServerApp" -q

# 新开终端，启动客户端
mvn exec:java -Dexec.mainClass="com.rental.client.ui.ClientUI" -q
```

- [ ] **步骤 3：Commit**

```bash
git add pom.xml
git commit -m "chore: 添加Maven配置和Gson依赖"
```

---

## 七、验证步骤

### 服务端启动验证
```bash
mvn exec:java -Dexec.mainClass="com.rental.server.ServerApp" -q
# 预期输出：
# ========== 租车系统服务端 启动中 ==========
# 车辆数据初始化完成
# 服务端启动成功，监听端口: 8888
# 等待客户端连接...
```

### 客户端连接验证
```bash
mvn exec:java -Dexec.mainClass="com.rental.client.ui.ClientUI" -q
# 预期：客户端窗口打开，显示"连接状态: 已连接"
```

---

## 八、执行交接

**计划已完成并保存到 `docs/superpowers/plans/2026-04-22-zuche-restructure.md`**

### 两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

**选哪种方式？**