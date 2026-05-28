const fs = require('fs');
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, 
        AlignmentType, HeadingLevel, BorderStyle, WidthType, ShadingType, 
        VerticalAlign, LevelFormat, PageBreak } = require('docx');

// 创建需求文档
function createRequirementsDoc() {
    const doc = new Document({
        styles: {
            default: { document: { run: { font: "Arial", size: 24 } } },
            paragraphStyles: [
                { id: "Title", name: "Title", basedOn: "Normal",
                    run: { size: 56, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 240, after: 120 }, alignment: AlignmentType.CENTER } },
                { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 32, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 240, after: 240 }, outlineLevel: 0 } },
                { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 28, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 180, after: 180 }, outlineLevel: 1 } },
                { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 24, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 120, after: 120 }, outlineLevel: 2 } }
            ]
        },
        numbering: {
            config: [
                { reference: "bullet-list",
                    levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
                { reference: "numbered-list",
                    levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] }
            ]
        },
        sections: [{
            properties: { page: { margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
            children: [
                new Paragraph({ heading: HeadingLevel.TITLE, children: [new TextRun("租车管理系统需求文档")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("1. 项目概述")] }),
                new Paragraph({ children: [new TextRun("基于命令行的租车管理系统，用于实现租车流程管理和租赁价格计算。系统支持不同车型的租赁管理，包括轿车、客车和卡车，并根据租赁天数应用不同的折扣规则。")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("2. 功能需求")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.1 核心功能")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("车辆管理")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("查看可租赁车辆列表")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("车辆信息管理（车型、日租金、车辆状态等）")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("租车流程")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("选择车辆")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("输入租赁天数")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("计算租赁费用")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("费用计算")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("根据车型和租赁天数计算基础费用")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("应用相应的折扣规则")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("四舍五入计算最终费用")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.2 价格计算规则")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.1 轿车（所有车型）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 7天：享9折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 30天：享8折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 150天：享7折")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.2 客车（所有车型）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 3天：享9折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 7天：享8折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 30天：享7折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 150天：享6折")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.3 卡车（所有车型）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 7天：享9.5折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 15天：享8.5折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 30天：享7.5折")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.4 费用计算公式")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("基础费用 = 日租金 × 租赁天数")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("折扣后费用 = 基础费用 × 折扣率")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("最终费用 = 折扣后费用（四舍五入到整数）")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("3. 非功能需求")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("3.1 性能要求")] }),
                new Paragraph({ children: [new TextRun("命令行响应速度快，操作流畅")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("3.2 可靠性要求")] }),
                new Paragraph({ children: [new TextRun("数据存储安全可靠")] }),
                new Paragraph({ children: [new TextRun("系统稳定性高，无崩溃情况")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("3.3 易用性要求")] }),
                new Paragraph({ children: [new TextRun("命令行界面简洁明了")] }),
                new Paragraph({ children: [new TextRun("操作流程简单直观")] }),
                new Paragraph({ children: [new TextRun("提供清晰的错误提示")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("4. 数据需求")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("4.1 车辆数据")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("车辆ID")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("车型（轿车/客车/卡车）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("具体车型名称")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("日租金")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("车辆状态（可租赁/已租赁）")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("5. 业务流程")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("浏览车辆：查看可租赁的车辆列表")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("选择车辆：选择心仪的车辆并确认")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("输入租赁信息：输入租赁天数等信息")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("费用计算：系统根据车型和租赁天数计算费用")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("展示费用：显示详细的费用计算结果")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("6. 命令行界面设计")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.1 主菜单")] }),
                new Paragraph({ children: [new TextRun("租车管理系统")] }),
                new Paragraph({ children: [new TextRun("1. 车辆管理")] }),
                new Paragraph({ children: [new TextRun("2. 租车流程")] }),
                new Paragraph({ children: [new TextRun("3. 退出系统")] }),
                new Paragraph({ children: [new TextRun("请选择操作：")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.2 租车流程界面")] }),
                new Paragraph({ children: [new TextRun("租车流程")] }),
                new Paragraph({ children: [new TextRun("1. 查看可租赁车辆")] }),
                new Paragraph({ children: [new TextRun("2. 选择车辆")] }),
                new Paragraph({ children: [new TextRun("3. 输入租赁天数")] }),
                new Paragraph({ children: [new TextRun("4. 计算费用")] }),
                new Paragraph({ children: [new TextRun("5. 返回主菜单")] }),
                new Paragraph({ children: [new TextRun("请选择操作：")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.3 费用计算展示")] }),
                new Paragraph({ children: [new TextRun("车辆信息：[车型] [具体车型]")] }),
                new Paragraph({ children: [new TextRun("日租金：¥[日租金]")] }),
                new Paragraph({ children: [new TextRun("租赁天数：[天数]天")] }),
                new Paragraph({ children: [new TextRun("基础费用：¥[基础费用]")] }),
                new Paragraph({ children: [new TextRun("折扣率：[折扣率]折")] }),
                new Paragraph({ children: [new TextRun("最终费用：¥[最终费用]")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("7. 实现计划")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.1 技术选型")] }),
                new Paragraph({ children: [new TextRun("开发语言：Java")] }),
                new Paragraph({ children: [new TextRun("数据存储：内存存储")] }),
                new Paragraph({ children: [new TextRun("界面：命令行界面")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.2 模块划分")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("车辆模块：车辆信息管理、车辆状态更新")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("计算模块：费用计算、折扣应用")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("主模块：系统入口、菜单管理")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("折扣策略模块：不同车型的折扣规则实现")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.3 开发步骤")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("搭建项目结构")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现基础数据结构")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现车辆管理功能")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现折扣策略模式")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现费用计算逻辑")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现主菜单和交互逻辑")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("测试和优化")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("8. 测试计划")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.1 功能测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试车辆管理功能")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试租车流程")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试费用计算准确性")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.2 边界测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试不同租赁天数的折扣计算")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试车辆状态切换")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.3 异常测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试输入错误数据的处理")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试系统稳定性")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("9. 项目交付")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("完整的命令行租车管理系统")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("系统使用说明文档")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试报告")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("10. 后续扩展")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("图形界面开发")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("数据库存储升级")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("在线支付功能")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("多分店管理")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("会员系统")] })
            ]
        }]
    });

    return doc;
}

// 创建架构设计文档
function createArchitectureDoc() {
    const doc = new Document({
        styles: {
            default: { document: { run: { font: "Arial", size: 24 } } },
            paragraphStyles: [
                { id: "Title", name: "Title", basedOn: "Normal",
                    run: { size: 56, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 240, after: 120 }, alignment: AlignmentType.CENTER } },
                { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 32, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 240, after: 240 }, outlineLevel: 0 } },
                { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 28, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 180, after: 180 }, outlineLevel: 1 } },
                { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 24, bold: true, color: "000000", font: "Arial" },
                    paragraph: { spacing: { before: 120, after: 120 }, outlineLevel: 2 } }
            ]
        },
        numbering: {
            config: [
                { reference: "bullet-list",
                    levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
                { reference: "numbered-list",
                    levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] }
            ]
        },
        sections: [{
            properties: { page: { margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
            children: [
                new Paragraph({ heading: HeadingLevel.TITLE, children: [new TextRun("租车管理系统架构及设计文档")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("1. 系统架构")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("1.1 整体架构")] }),
                new Paragraph({ children: [new TextRun("本系统采用简单的分层架构，适用于命令行应用的特点，主要分为三层：")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("表现层（命令行界面）：负责用户交互，处理命令行输入和输出，展示菜单和信息")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("业务逻辑层：实现核心业务逻辑，包括车辆管理、费用计算等功能")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("数据层（内存存储）：负责数据的存储和管理，使用内存存储")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("2. 模块设计")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.1 模块划分")] }),
                createModuleTable(),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.2 模块关系")] }),
                new Paragraph({ children: [new TextRun("主模块依赖车辆模块和计算模块")] }),
                new Paragraph({ children: [new TextRun("计算模块依赖折扣策略工厂")] }),
                new Paragraph({ children: [new TextRun("折扣策略工厂根据车型返回对应的折扣策略")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("3. 数据结构设计")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("3.1 车辆数据结构")] }),
                new Paragraph({ children: [new TextRun("Vehicle类包含以下属性：")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("id：车辆ID")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("type：车型（轿车/客车/卡车）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("model：具体车型名称")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("dailyRent：日租金")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("status：车辆状态（可租赁/已租赁）")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("3.2 数据存储")] }),
                new Paragraph({ children: [new TextRun("使用内存中的List<Vehicle>存储车辆数据。")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("4. 流程设计")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("4.1 系统启动流程")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("启动系统")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("初始化车辆数据")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("显示主菜单")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("用户选择操作")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("4.2 费用计算流程")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("输入车辆信息和租赁天数")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("获取车辆类型")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("通过工厂获取对应折扣策略")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("计算折扣率")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("计算基础费用")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("计算折扣后费用")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("四舍五入得到最终费用")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("返回计算结果")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("5. 类图设计")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("5.1 核心类图")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("Main类")] }),
                new Paragraph({ children: [new TextRun("功能：系统入口、菜单管理、用户交互")] }),
                new Paragraph({ children: [new TextRun("核心方法：")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("main()：系统主方法")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("showMainMenu()：显示主菜单")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("showRentalMenu()：显示租车流程菜单")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("handleMainMenuChoice()：处理主菜单选择")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("handleRentalMenuChoice()：处理租车流程菜单选择")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("Vehicle类")] }),
                new Paragraph({ children: [new TextRun("功能：封装车辆属性")] }),
                new Paragraph({ children: [new TextRun("属性：id、type、model、dailyRent、status")] }),
                new Paragraph({ children: [new TextRun("方法：提供getter和setter方法")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("VehicleManager类")] }),
                new Paragraph({ children: [new TextRun("功能：管理车辆列表")] }),
                new Paragraph({ children: [new TextRun("核心方法：")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("initializeVehicles()：初始化默认车辆数据")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("getAvailableVehicles()：获取可租赁车辆列表")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("getVehicleById()：根据ID获取车辆")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("updateVehicleStatus()：更新车辆状态")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("RentalCalculator类")] }),
                new Paragraph({ children: [new TextRun("功能：计算租赁费用")] }),
                new Paragraph({ children: [new TextRun("核心方法：")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("getDiscountRate()：获取折扣率")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("calculateRentalFee()：计算最终租赁费用")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("DiscountStrategy接口")] }),
                new Paragraph({ children: [new TextRun("功能：定义折扣计算方法")] }),
                new Paragraph({ children: [new TextRun("方法：getDiscountRate(int days)")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("具体策略类")] }),
                new Paragraph({ children: [new TextRun("CarDiscountStrategy：轿车折扣策略")] }),
                new Paragraph({ children: [new TextRun("BusDiscountStrategy：客车折扣策略")] }),
                new Paragraph({ children: [new TextRun("TruckDiscountStrategy：卡车折扣策略")] }),
                new Paragraph({ children: [new TextRun("DefaultDiscountStrategy：默认折扣策略")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("DiscountStrategyFactory类")] }),
                new Paragraph({ children: [new TextRun("功能：根据车型获取对应折扣策略")] }),
                new Paragraph({ children: [new TextRun("方法：getDiscountStrategy(String vehicleType)")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("6. 设计模式应用")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.1 策略模式")] }),
                new Paragraph({ children: [new TextRun("应用场景：不同车型的折扣规则不同")] }),
                new Paragraph({ children: [new TextRun("实现方式：创建DiscountStrategy接口，不同车型实现各自的折扣策略")] }),
                new Paragraph({ children: [new TextRun("优点：")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("封装了不同车型的折扣规则")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("易于添加新车型的折扣策略")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("提高了代码的可维护性")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.2 工厂模式")] }),
                new Paragraph({ children: [new TextRun("应用场景：根据车型动态选择折扣策略")] }),
                new Paragraph({ children: [new TextRun("实现方式：创建DiscountStrategyFactory工厂类，根据车型返回对应的折扣策略")] }),
                new Paragraph({ children: [new TextRun("优点：")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("封装了策略的创建逻辑")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("客户端代码与具体策略解耦")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("提高了系统的灵活性")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("7. 技术实现细节")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.1 数据存储")] }),
                new Paragraph({ children: [new TextRun("使用内存中的List<Vehicle>存储车辆数据")] }),
                new Paragraph({ children: [new TextRun("程序启动时自动初始化默认车辆数据，包括轿车、客车和卡车")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.2 错误处理")] }),
                new Paragraph({ children: [new TextRun("对用户输入进行验证，确保输入的租赁天数为正整数")] }),
                new Paragraph({ children: [new TextRun("处理车辆ID不存在的情况")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.3 扩展性")] }),
                new Paragraph({ children: [new TextRun("采用策略模式和工厂模式实现折扣规则管理")] }),
                new Paragraph({ children: [new TextRun("新增车型时，只需要：")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("创建对应的折扣策略类")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("在DiscountStrategyFactory中添加策略映射")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("在VehicleManager中添加车辆数据")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("8. 测试计划")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.1 功能测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试车辆管理功能")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试租车流程")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试费用计算准确性")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.2 边界测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试不同租赁天数的折扣计算")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试车辆状态切换")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.3 异常测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试输入非数字的租赁天数")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试输入负数的租赁天数")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试输入不存在的车辆ID")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("9. 部署与运行")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("9.1 环境要求")] }),
                new Paragraph({ children: [new TextRun("JDK 8+ 环境")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("9.2 运行步骤")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("确保安装了JDK 8+")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("进入项目根目录")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("编译所有Java文件：javac -d bin src/*.java")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("运行系统：java -cp bin Main")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("9.3 数据初始化")] }),
                new Paragraph({ children: [new TextRun("程序启动时，系统会自动初始化默认车辆数据到内存中")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("10. 后续扩展")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("10.1 功能扩展")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("添加车辆状态管理（租赁/归还）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("添加订单管理功能")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("添加用户管理功能")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("10.2 技术扩展")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("迁移到数据库存储（如MySQL）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("开发图形界面（如Swing或JavaFX）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("添加网络功能，支持远程访问")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("11. 总结")] }),
                new Paragraph({ children: [new TextRun("本架构设计文档详细说明了租车管理系统的架构、模块划分、数据结构和实现细节。系统采用简单的分层架构，模块化设计，便于实现和维护。通过命令行界面，用户可以方便地浏览车辆、选择车辆、输入租赁天数并计算租赁费用。")] }),
                new Paragraph({ children: [new TextRun("系统使用策略模式和工厂模式实现了不同车型的折扣规则管理，具有良好的扩展性。当未来需要添加新车型时，只需要创建对应的折扣策略类并在工厂中注册即可。")] }),
                new Paragraph({ children: [new TextRun("该设计满足了需求文档中的所有功能要求，并且具有良好的扩展性，可以根据需要添加新的功能和技术特性。")] })
            ]
        }]
    });

    return doc;
}

function createModuleTable() {
    const tableBorder = { style: BorderStyle.SINGLE, size: 1, color: "CCCCCC" };
    const cellBorders = { top: tableBorder, bottom: tableBorder, left: tableBorder, right: tableBorder };

    return new Table({
        columnWidths: [3120, 3120, 3120],
        margins: { top: 100, bottom: 100, left: 180, right: 180 },
        rows: [
            new TableRow({
                tableHeader: true,
                children: [
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        shading: { fill: "D5E8F0", type: ShadingType.CLEAR },
                        verticalAlign: VerticalAlign.CENTER,
                        children: [new Paragraph({ 
                            alignment: AlignmentType.CENTER,
                            children: [new TextRun({ text: "模块名称", bold: true, size: 22 })]
                        })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        shading: { fill: "D5E8F0", type: ShadingType.CLEAR },
                        verticalAlign: VerticalAlign.CENTER,
                        children: [new Paragraph({ 
                            alignment: AlignmentType.CENTER,
                            children: [new TextRun({ text: "功能描述", bold: true, size: 22 })]
                        })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        shading: { fill: "D5E8F0", type: ShadingType.CLEAR },
                        verticalAlign: VerticalAlign.CENTER,
                        children: [new Paragraph({ 
                            alignment: AlignmentType.CENTER,
                            children: [new TextRun({ text: "文件路径", bold: true, size: 22 })]
                        })]
                    })
                ]
            }),
            new TableRow({
                children: [
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("主模块")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("系统入口，菜单管理，用户交互")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("src/Main.java")] })]
                    })
                ]
            }),
            new TableRow({
                children: [
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("车辆模块")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("车辆信息管理，车辆状态更新")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("src/Vehicle.java, src/VehicleManager.java")] })]
                    })
                ]
            }),
            new TableRow({
                children: [
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("计算模块")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("费用计算，折扣应用")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("src/RentalCalculator.java")] })]
                    })
                ]
            }),
            new TableRow({
                children: [
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("折扣策略模块")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("不同车型的折扣规则实现")] })]
                    }),
                    new TableCell({
                        borders: cellBorders,
                        width: { size: 3120, type: WidthType.DXA },
                        children: [new Paragraph({ children: [new TextRun("src/DiscountStrategy.java, src/CarDiscountStrategy.java, src/BusDiscountStrategy.java, src/TruckDiscountStrategy.java, src/DefaultDiscountStrategy.java, src/DiscountStrategyFactory.java")] })]
                    })
                ]
            })
        ]
    });
}

// 生成文档
async function generateDocuments() {
    try {
        // 生成需求文档
        const requirementsDoc = createRequirementsDoc();
        const requirementsBuffer = await Packer.toBuffer(requirementsDoc);
        fs.writeFileSync("docs/需求文档.docx", requirementsBuffer);
        console.log("需求文档已生成: docs/需求文档.docx");

        // 生成架构设计文档
        const architectureDoc = createArchitectureDoc();
        const architectureBuffer = await Packer.toBuffer(architectureDoc);
        fs.writeFileSync("docs/架构及设计文档.docx", architectureBuffer);
        console.log("架构及设计文档已生成: docs/架构及设计文档.docx");

    } catch (error) {
        console.error("生成文档时出错:", error);
    }
}

generateDocuments();
