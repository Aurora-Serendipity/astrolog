const fs = require('fs');
const { execSync } = require('child_process');

// 定义所有图表
const diagrams = [
    {
        name: 'system_architecture',
        content: `graph TB
    subgraph 表现层
        A[命令行界面]
        B[用户交互]
    end
    
    subgraph 业务逻辑层
        C[主模块 Main]
        D[车辆管理模块 VehicleManager]
        E[费用计算模块 RentalCalculator]
        F[折扣策略工厂 DiscountStrategyFactory]
    end
    
    subgraph 数据层
        G[内存存储 List<Vehicle>]
    end
    
    A --> C
    B --> C
    C --> D
    C --> E
    E --> F
    D --> G
    E --> G`
    },
    {
        name: 'module_relationship',
        content: `graph LR
    A[Main 主模块] --> B[VehicleManager 车辆模块]
    A --> C[RentalCalculator 计算模块]
    C --> D[DiscountStrategyFactory 折扣策略工厂]
    D --> E[CarDiscountStrategy 轿车策略]
    D --> F[BusDiscountStrategy 客车策略]
    D --> G[TruckDiscountStrategy 卡车策略]
    D --> H[DefaultDiscountStrategy 默认策略]`
    },
    {
        name: 'system_startup_flow',
        content: `graph TD
    A[启动系统] --> B[初始化车辆数据]
    B --> C[显示主菜单]
    C --> D{用户选择}
    D -->|1. 车辆管理| E[显示车辆列表]
    D -->|2. 租车流程| F[进入租车流程]
    D -->|3. 退出系统| G[退出系统]
    E --> C
    F --> H[显示可租赁车辆]
    H --> I[选择车辆]
    I --> J[输入租赁天数]
    J --> K[计算费用]
    K --> L[显示费用结果]
    L --> M{是否继续}
    M -->|是| H
    M -->|否| C`
    },
    {
        name: 'fee_calculation_flow',
        content: `graph TD
    A[输入车辆信息和租赁天数] --> B[获取车辆类型]
    B --> C[通过工厂获取对应折扣策略]
    C --> D[计算折扣率]
    D --> E[计算基础费用]
    E --> F[计算折扣后费用]
    F --> G[四舍五入得到最终费用]
    G --> H[返回计算结果]`
    },
    {
        name: 'discount_strategy_flow',
        content: `graph TD
    A[开始] --> B[输入车型]
    B --> C{车型类型}
    C -->|轿车| D[使用轿车折扣策略]
    C -->|客车| E[使用客车折扣策略]
    C -->|卡车| F[使用卡车折扣策略]
    C -->|其他| G[使用默认折扣策略]
    D --> H[计算折扣率]
    E --> H
    F --> H
    G --> H
    H --> I[返回折扣率]`
    },
    {
        name: 'class_diagram',
        content: `classDiagram
    class Main {
        -Scanner scanner
        -VehicleManager vehicleManager
        -Vehicle selectedVehicle
        -int rentalDays
        +main(String[] args)
        +showMainMenu()
        +showRentalMenu()
        +handleMainMenuChoice(String choice)
        +handleRentalMenuChoice(String choice)
        +manageVehicles()
        +rentalProcess()
    }
    
    class Vehicle {
        -String id
        -String type
        -String model
        -double dailyRent
        -String status
        +Vehicle(String id, String type, String model, double dailyRent, String status)
        +getId() String
        +getType() String
        +getModel() String
        +getDailyRent() double
        +getStatus() String
        +setId(String id)
        +setType(String type)
        +setModel(String model)
        +setDailyRent(double dailyRent)
        +setStatus(String status)
    }
    
    class VehicleManager {
        -List~Vehicle~ vehicles
        +VehicleManager()
        +initializeVehicles()
        +getAvailableVehicles() List~Vehicle~
        +getVehicleById(String vehicleId) Vehicle
        +updateVehicleStatus(String vehicleId, String status) boolean
        +getAllVehicles() List~Vehicle~
    }
    
    class RentalCalculator {
        +getDiscountRate(String vehicleType, int days) double
        +calculateRentalFee(String vehicleType, double dailyRent, int days) int
    }
    
    class DiscountStrategy {
        <<interface>>
        +getDiscountRate(int days) double
    }
    
    class CarDiscountStrategy {
        +getDiscountRate(int days) double
    }
    
    class BusDiscountStrategy {
        +getDiscountRate(int days) double
    }
    
    class TruckDiscountStrategy {
        +getDiscountRate(int days) double
    }
    
    class DefaultDiscountStrategy {
        +getDiscountRate(int days) double
    }
    
    class DiscountStrategyFactory {
        +getDiscountStrategy(String vehicleType) DiscountStrategy
    }
    
    Main --> VehicleManager
    Main --> RentalCalculator
    VehicleManager --> Vehicle
    RentalCalculator --> DiscountStrategyFactory
    DiscountStrategyFactory --> DiscountStrategy
    DiscountStrategy <|.. CarDiscountStrategy
    DiscountStrategy <|.. BusDiscountStrategy
    DiscountStrategy <|.. TruckDiscountStrategy
    DiscountStrategy <|.. DefaultDiscountStrategy`
    },
    {
        name: 'rental_process_sequence',
        content: `sequenceDiagram
    participant User as 用户
    participant Main as 主模块
    participant VehicleManager as 车辆管理模块
    participant RentalCalculator as 计算模块
    participant DiscountStrategyFactory as 折扣策略工厂
    participant DiscountStrategy as 折扣策略
    
    User->>Main: 选择租车流程
    Main->>VehicleManager: 获取可租赁车辆
    VehicleManager-->>Main: 返回车辆列表
    Main-->>User: 显示车辆列表
    User->>Main: 选择车辆
    Main->>VehicleManager: 根据ID获取车辆
    VehicleManager-->>Main: 返回车辆信息
    Main-->>User: 显示已选择车辆
    User->>Main: 输入租赁天数
    Main->>RentalCalculator: 计算租赁费用
    RentalCalculator->>DiscountStrategyFactory: 获取折扣策略
    DiscountStrategyFactory->>DiscountStrategy: 创建对应策略
    DiscountStrategy-->>DiscountStrategyFactory: 返回策略实例
    DiscountStrategyFactory-->>RentalCalculator: 返回折扣策略
    RentalCalculator->>DiscountStrategy: 计算折扣率
    DiscountStrategy-->>RentalCalculator: 返回折扣率
    RentalCalculator-->>Main: 返回计算结果
    Main-->>User: 显示费用详情`
    },
    {
        name: 'vehicle_management_sequence',
        content: `sequenceDiagram
    participant User as 用户
    participant Main as 主模块
    participant VehicleManager as 车辆管理模块
    participant Vehicle as 车辆对象
    
    User->>Main: 选择车辆管理
    Main->>VehicleManager: 获取可租赁车辆
    VehicleManager->>Vehicle: 遍历车辆列表
    Vehicle-->>VehicleManager: 返回车辆信息
    VehicleManager-->>Main: 返回可租赁车辆列表
    Main-->>User: 显示车辆列表`
    },
    {
        name: 'fee_calculation_sequence',
        content: `sequenceDiagram
    participant Main as 主模块
    participant RentalCalculator as 计算模块
    participant Factory as 折扣策略工厂
    participant Strategy as 折扣策略
    
    Main->>RentalCalculator: calculateRentalFee(车型, 日租金, 天数)
    RentalCalculator->>RentalCalculator: 计算基础费用 = 日租金 × 天数
    RentalCalculator->>Factory: getDiscountStrategy(车型)
    Factory-->>RentalCalculator: 返回对应策略
    RentalCalculator->>Strategy: getDiscountRate(天数)
    Strategy-->>RentalCalculator: 返回折扣率
    RentalCalculator->>RentalCalculator: 计算折扣后费用 = 基础费用 × 折扣率
    RentalCalculator->>RentalCalculator: 四舍五入得到最终费用
    RentalCalculator-->>Main: 返回最终费用`
    },
    {
        name: 'data_flow',
        content: `graph LR
    A[用户输入] --> B[主模块]
    B --> C[车辆管理模块]
    B --> D[费用计算模块]
    C --> E[车辆数据]
    D --> F[折扣策略]
    D --> E
    E --> G[内存存储]
    F --> H[计算结果]
    H --> B
    B --> I[用户输出]`
    }
];

// 创建图表目录
const diagramsDir = 'diagrams';
if (!fs.existsSync(diagramsDir)) {
    fs.mkdirSync(diagramsDir);
}

// 生成每个图表
diagrams.forEach(diagram => {
    const mmdFile = `${diagramsDir}/${diagram.name}.mmd`;
    const pngFile = `${diagramsDir}/${diagram.name}.png`;
    
    // 写入mermaid文件
    fs.writeFileSync(mmdFile, diagram.content);
    
    try {
        // 使用mmdc命令生成PNG图片
        execSync(`mmdc -i ${mmdFile} -o ${pngFile}`, { stdio: 'inherit' });
        console.log(`✓ 已生成: ${pngFile}`);
    } catch (error) {
        console.error(`✗ 生成失败: ${pngFile}`, error.message);
    }
});

console.log('\n所有图表生成完成！');
