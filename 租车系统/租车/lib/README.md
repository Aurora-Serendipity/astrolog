# Gson 依赖配置说明

由于项目不再使用 Maven 管理依赖，需要手动下载 Gson JAR 文件并放入 lib 目录。

## 下载步骤
1. 打开浏览器访问 Maven Central 仓库：https://mvnrepository.com/artifact/com.google.code.gson/gson/2.10.1
2. 点击 "Download (JAR)" 按钮下载 gson-2.10.1.jar 文件
3. 将下载的 gson-2.10.1.jar 文件复制到项目的 lib 目录中

## 编译和运行命令

### 编译命令
```bash
# 编译所有 Java 文件
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/rental/**/*.java
```

### 运行服务端
```bash
# 启动服务端
java -cp "bin;lib/*" com.rental.server.ServerApp
```

### 运行客户端
```bash
# 启动客户端
java -cp "bin;lib/*" com.rental.client.ui.ClientUI
```

## 注意事项
- 确保 JDK 版本为 1.8 或更高
- 确保 lib 目录中存在 gson-2.10.1.jar 文件
- 编译时使用 -encoding UTF-8 确保中文注释正常处理
- 运行时需要同时指定 bin 目录和 lib 目录到 classpath 中