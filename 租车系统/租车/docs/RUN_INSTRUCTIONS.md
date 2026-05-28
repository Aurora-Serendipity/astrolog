# 租车系统 v3.0 运行说明

## 系统要求
- JDK 1.8 或更高版本
- MySQL 8.0 或更高版本

## 系统架构
本系统采用前后端分离架构：
- **后端（Server）**：Java Socket 服务端，使用多线程处理多个客户端连接，数据存储到 MySQL 数据库
- **前端（Client）**：Java Swing 客户端，通过 Socket 与服务端通信

## 启动顺序
1. **确保 MySQL 数据库已启动**
2. **必须先启动服务端**
3. **再启动客户端**

## 准备工作

### 1. 下载依赖 JAR 文件并放入 lib 目录
- **Gson JAR**：https://mvnrepository.com/artifact/com.google.code.gson/gson/2.10.1
  - 文件名：gson-2.10.1.jar
  - 放置位置：lib/gson-2.10.1.jar

- **MySQL JDBC 驱动**：https://mvnrepository.com/artifact/com.mysql/mysql-connector-j/8.0.33
  - 文件名：mysql-connector-j-8.0.33.jar
  - 放置位置：lib/mysql-connector-j-8.0.33.jar

### 2. 数据库配置
编辑 `src/db.properties` 文件，配置数据库连接信息：
```properties
# 数据库连接URL
jdbc.url=jdbc:mysql://localhost:3306/zuche?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false

# 数据库用户名
jdbc.username=root

# 数据库密码
jdbc.password=root

# 连接池配置
pool.maxConnections=10
pool.minConnections=2
pool.initialConnections=5
```

### 3. 数据库准备
```sql
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS zuche CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 启动方式

### 1. 编译项目
```bash
# 编译所有 Java 文件
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/rental/**/*.java src/db.properties
```

### 2. 启动服务端
```bash
# 启动服务端（自动创建表结构并初始化数据）
java -cp "bin;lib/*" com.rental.server.ServerApp
```

### 3. 启动客户端
```bash
# 启动客户端
java -cp "bin;lib/*" com.rental.client.ui.ClientUI
```

### 4. 数据迁移（可选）
如果需要将旧的 JSON 文件数据迁移到数据库：
```bash
java -cp "bin;lib/*" com.rental.repository.DataMigration
```

## 系统功能

### 服务端功能
- 监听端口 8888，处理客户端连接
- 支持多客户端并发访问（线程池处理）
- 车辆数据初始化（默认7辆车辆）
- 车辆管理：查询、添加、更新、删除
- 租赁管理：租车、还车、查询记录
- 数据持久化到 MySQL 数据库

### 客户端功能
- 连接服务端
- 车辆列表查看
- 租车操作
- 还车操作
- 租赁记录查询
- 车辆详情查看

## 数据存储
数据存储在 MySQL 数据库 `zuche` 中：
- `vehicles` 表 - 车辆数据
- `rental_records` 表 - 租赁记录

## 通信协议
客户端与服务端通过 JSON 格式的 TCP Socket 消息通信，端口号：8888

## 常见问题

### 1. 连接失败
- 检查服务端是否已启动
- 检查防火墙是否阻止端口 8888
- 检查网络连接

### 2. 数据库连接失败
- 检查 MySQL 服务是否已启动
- 检查 `src/db.properties` 配置是否正确
- 检查数据库用户名和密码是否正确
- 确保数据库 `zuche` 已创建

### 3. 编译错误
- 确保 JDK 版本为 1.8 或更高
- 确保所有依赖 JAR 文件已放入 lib 目录

### 4. 数据问题
- 如果需要重新初始化数据，删除数据库表后重启服务端

## 系统特点
- **多线程**：服务端使用线程池处理并发请求
- **网络通信**：基于 Java Socket 的 TCP 通信
- **JDBC 数据库**：使用 MySQL 数据库存储数据
- **连接池**：实现简单的数据库连接池管理
- **前后端分离**：客户端和服务端独立运行
- **用户友好**：Swing 图形界面，操作简单直观
