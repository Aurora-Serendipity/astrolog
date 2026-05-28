const fs = require('fs');
const { execSync } = require('child_process');

// 定义需求文档需要的图表
const diagrams = [
    {
        name: 'business_process',
        content: `graph TD
    A[客户浏览车辆] --> B[选择车辆]
    B --> C[输入租赁天数]
    C --> D[系统计算费用]
    D --> E[显示费用详情]
    E --> F[客户确认]
    F --> G[完成租车]
    G --> H[生成订单]`
    },
    {
        name: 'price_calculation_rules',
        content: `graph LR
    subgraph 轿车折扣规则
        A1[租赁天数 > 7天] --> B1[9折]
        A2[租赁天数 > 30天] --> B2[8折]
        A3[租赁天数 > 150天] --> B3[7折]
    end
    
    subgraph 客车折扣规则
        C1[租赁天数 >= 3天] --> D1[9折]
        C2[租赁天数 >= 7天] --> D2[8折]
        C3[租赁天数 >= 30天] --> D3[7折]
        C4[租赁天数 >= 150天] --> D4[6折]
    end
    
    subgraph 卡车折扣规则
        E1[租赁天数 > 7天] --> F1[9.5折]
        E2[租赁天数 > 15天] --> F2[8.5折]
        E3[租赁天数 > 30天] --> F3[7.5折]
    end`
    },
    {
        name: 'system_features',
        content: `graph TD
    A[租车管理系统] --> B[车辆管理]
    A --> C[租车流程]
    A --> D[费用计算]
    B --> B1[查看可租赁车辆]
    B --> B2[车辆信息管理]
    C --> C1[选择车辆]
    C --> C2[输入租赁天数]
    C --> C3[计算费用]
    D --> D1[基础费用计算]
    D --> D2[折扣应用]
    D --> D3[最终费用计算]`
    },
    {
        name: 'cli_interface_flow',
        content: `graph TD
    A[主菜单] --> B{选择操作}
    B -->|1. 车辆管理| C[显示可租赁车辆]
    B -->|2. 租车流程| D[租车流程菜单]
    B -->|3. 退出系统| E[退出]
    C --> A
    D --> F{租车操作}
    F -->|1. 查看可租赁车辆| C
    F -->|2. 选择车辆| G[输入车辆ID]
    F -->|3. 输入租赁天数| H[输入天数]
    F -->|4. 计算费用| I[显示费用]
    F -->|5. 返回主菜单| A
    G --> F
    H --> F
    I --> F`
    },
    {
        name: 'data_structure',
        content: `graph TD
    A[Vehicle 类] --> B[id: 车辆ID]
    A --> C[type: 车型]
    A --> D[model: 具体车型]
    A --> E[dailyRent: 日租金]
    A --> F[status: 车辆状态]
    B --> G[String 类型]
    C --> H[String 类型]
    D --> I[String 类型]
    E --> J[double 类型]
    F --> K[String 类型]`
    },
    {
        name: 'system_components',
        content: `graph TB
    subgraph 核心功能
        A[车辆管理]
        B[租车流程]
        C[费用计算]
    end
    
    subgraph 支持功能
        D[数据存储]
        E[用户交互]
        F[错误处理]
    end
    
    A --> D
    B --> E
    B --> C
    C --> D
    E --> F`
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

console.log('\n需求文档图表生成完成！');
