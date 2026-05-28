const fs = require('fs');
const path = require('path');
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, 
        AlignmentType, HeadingLevel, BorderStyle, WidthType, ShadingType, 
        VerticalAlign, LevelFormat, ImageRun } = require('docx');

function createRequirementsDoc() {
    const doc = new Document({
        styles: {
            default: { document: { run: { font: "Arial", size: 22 } } },
            paragraphStyles: [
                { id: "Title", name: "Title", basedOn: "Normal",
                    run: { size: 56, bold: true, color: "000000" },
                    paragraph: { spacing: { before: 240, after: 120 }, alignment: AlignmentType.CENTER } },
                { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 28, bold: true, color: "000000" },
                    paragraph: { spacing: { before: 240, after: 180 }, outlineLevel: 0 } },
                { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 24, bold: true, color: "000000" },
                    paragraph: { spacing: { before: 180, after: 120 }, outlineLevel: 1 } },
                { id: "Normal", name: "Normal", basedOn: "Normal",
                    run: { size: 22, color: "000000" },
                    paragraph: { spacing: { after: 100 } } }
            ]
        },
        numbering: {
            config: [
                { reference: "num",
                    levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
                { reference: "bullet",
                    levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] }
            ]
        },
        sections: [{
            properties: { page: { margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
            children: [
                new Paragraph({ heading: HeadingLevel.TITLE, children: [new TextRun({ text: "租车管理系统需求规格说明书", bold: true })] }),
                new Paragraph({ children: [new TextRun({ text: "文档编号: SRS-001  版本: 2.0  编制日期: 2026-04-03" })] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "1. 引言" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "1.1 项目背景" })] }),
                new Paragraph({ children: [new TextRun("本项目是一个基于命令行的租车管理系统，旨在为租车公司提供车辆租赁管理和费用计算功能。系统采用Java语言开发，支持轿车、客车、卡车三种车型的租赁管理，并根据租赁天数应用不同的折扣策略。")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "1.2 术语定义" })] }),
                new Paragraph({ children: [new TextRun("轿车: 四轮载客汽车，用于个人或商务出行")] }),
                new Paragraph({ children: [new TextRun("客车: 大型载客汽车，用于团队运输")] }),
                new Paragraph({ children: [new TextRun("卡车: 载货汽车，用于货物运输")] }),
                new Paragraph({ children: [new TextRun("日租金: 每24小时的租赁费用")] }),
                new Paragraph({ children: [new TextRun("折扣率: 实际支付金额与基础费用的比例")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "2. 总体描述" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "2.1 系统概述" })] }),
                new Paragraph({ children: [new TextRun("租车管理系统是一个命令行应用软件，提供车辆浏览、选择、租赁天数输入和费用计算功能。系统采用内存存储数据，无需数据库支持。")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "2.2 系统约束" })] }),
                new Paragraph({ children: [new TextRun("技术约束: JDK 8+ 环境运行，纯命令行界面")] }),
                new Paragraph({ children: [new TextRun("数据约束: 内存存储，程序退出后数据不持久化")] }),
                new Paragraph({ children: [new TextRun("功能约束: 仅支持基本的租车流程，不含用户管理、订单管理")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "3. 功能需求" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "3.1 功能列表" })] }),
                new Paragraph({ numbering: { reference: "num", level: 0 }, children: [new TextRun("F01 车辆列表展示: 显示所有可租赁车辆的详细信息")] }),
                new Paragraph({ numbering: { reference: "num", level: 0 }, children: [new TextRun("F02 车辆选择: 根据车辆ID选择目标车辆")] }),
                new Paragraph({ numbering: { reference: "num", level: 0 }, children: [new TextRun("F03 租赁天数输入: 接收用户输入的租赁天数")] }),
                new Paragraph({ numbering: { reference: "num", level: 0 }, children: [new TextRun("F04 费用计算: 根据车型和天数计算最终费用")] }),
                new Paragraph({ numbering: { reference: "num", level: 0 }, children: [new TextRun("F05 菜单导航: 提供主菜单和租车流程菜单导航")] }),
                new Paragraph({ numbering: { reference: "num", level: 0 }, children: [new TextRun("F06 退出系统: 安全退出应用程序")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "3.2 业务流程" })] }),
                new Paragraph({ children: [new TextRun("图3-1 租车业务流程图")] }),
                new Paragraph({
                    children: [new ImageRun({
                        data: fs.readFileSync(path.join(__dirname, '..', 'diagrams', 'system_startup_flow.png')),
                        transformation: { width: 400, height: 300 },
                        type: "png"
                    })]
                }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "4. 折扣规则需求" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "4.1 轿车折扣规则" })] }),
                new Paragraph({ children: [new TextRun("1-7天: 100%, 8-30天: 90%, 31-150天: 80%, >150天: 70%")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "4.2 客车折扣规则" })] }),
                new Paragraph({ children: [new TextRun("1-2天: 100%, 3-6天: 90%, 7-29天: 80%, 30-149天: 70%, >=150天: 60%")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "4.3 卡车折扣规则" })] }),
                new Paragraph({ children: [new TextRun("1-7天: 100%, 8-15天: 95%, 16-30天: 85%, >30天: 75%")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "5. 数据需求" })] }),
                new Paragraph({ children: [new TextRun("车辆数据: id(车辆ID), type(车型), model(具体车型), dailyRent(日租金), status(状态)")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "6. 界面需求" })] }),
                new Paragraph({ children: [new TextRun("主菜单: 1.车辆管理 2.租车流程 3.退出系统")] }),
                new Paragraph({ children: [new TextRun("租车流程: 1.查看车辆 2.选择车辆 3.输入天数 4.计算费用 5.返回")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "7. 非功能需求" })] }),
                new Paragraph({ children: [new TextRun("性能: 启动<2秒，响应<100ms")] }),
                new Paragraph({ children: [new TextRun("可靠性: 数据存储安全，系统稳定")] }),
                new Paragraph({ children: [new TextRun("易用性: 界面简洁，流程直观，错误提示清晰")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "8. 测试需求" })] }),
                new Paragraph({ children: [new TextRun("功能测试: 车辆列表、车辆选择、天数输入、费用计算")] }),
                new Paragraph({ children: [new TextRun("边界测试: 各折扣档位临界值")] }),
                new Paragraph({ children: [new TextRun("异常测试: 无效输入处理")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "9. 验收标准" })] }),
                new Paragraph({ children: [new TextRun("系统能正常启动并显示主菜单")] }),
                new Paragraph({ children: [new TextRun("能查看可租赁车辆列表（7辆）")] }),
                new Paragraph({ children: [new TextRun("能通过ID选择车辆")] }),
                new Paragraph({ children: [new TextRun("费用计算结果正确")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "10. 后续扩展" })] }),
                new Paragraph({ children: [new TextRun("功能扩展: 订单管理、用户管理、车辆归还")] }),
                new Paragraph({ children: [new TextRun("技术扩展: 数据库存储、图形界面、网络功能")] }),
            ]
        }]
    });
    return doc;
}

function createArchitectureDoc() {
    const doc = new Document({
        styles: {
            default: { document: { run: { font: "Arial", size: 22 } } },
            paragraphStyles: [
                { id: "Title", name: "Title", basedOn: "Normal",
                    run: { size: 56, bold: true, color: "000000" },
                    paragraph: { spacing: { before: 240, after: 120 }, alignment: AlignmentType.CENTER } },
                { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 28, bold: true, color: "000000" },
                    paragraph: { spacing: { before: 240, after: 180 }, outlineLevel: 0 } },
                { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
                    run: { size: 24, bold: true, color: "000000" },
                    paragraph: { spacing: { before: 180, after: 120 }, outlineLevel: 1 } },
                { id: "Normal", name: "Normal", basedOn: "Normal",
                    run: { size: 22, color: "000000" },
                    paragraph: { spacing: { after: 100 } } }
            ]
        },
        numbering: {
            config: [
                { reference: "num",
                    levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
                { reference: "bullet",
                    levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
                        style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] }
            ]
        },
        sections: [{
            properties: { page: { margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
            children: [
                new Paragraph({ heading: HeadingLevel.TITLE, children: [new TextRun({ text: "租车管理系统架构设计说明书", bold: true })] }),
                new Paragraph({ children: [new TextRun({ text: "文档编号: SDS-001  版本: 2.0  编制日期: 2026-04-03" })] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "1. 系统概述" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "1.1 设计目标" })] }),
                new Paragraph({ children: [new TextRun("本设计文档详细描述租车管理系统的架构设计，包括系统架构、模块划分、数据结构、类图设计、流程设计等，确保开发团队对系统有统一的理解。")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "2. 系统架构" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "2.1 整体架构" })] }),
                new Paragraph({ children: [new TextRun("本系统采用经典的三层架构设计: 表现层(CLI界面) -> 业务逻辑层 -> 数据层(内存存储)")] }),
                new Paragraph({
                    children: [new ImageRun({
                        data: fs.readFileSync(path.join(__dirname, '..', 'diagrams', 'system_architecture.png')),
                        transformation: { width: 450, height: 250 },
                        type: "png"
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "2.2 层次说明" })] }),
                new Paragraph({ children: [new TextRun("表现层: 用户交互、菜单导航 (Main)")] }),
                new Paragraph({ children: [new TextRun("业务逻辑层: 核心业务逻辑处理 (VehicleManager, RentalCalculator, DiscountStrategyFactory)")] }),
                new Paragraph({ children: [new TextRun("数据层: 数据存储和管理 (List<Vehicle>)")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "3. 包结构设计" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "3.1 包划分" })] }),
                new Paragraph({ children: [new TextRun("com.rental.model: 数据模型 (Vehicle)")] }),
                new Paragraph({ children: [new TextRun("com.rental.strategy: 折扣策略 (策略模式)")] }),
                new Paragraph({ children: [new TextRun("com.rental.service: 业务服务 (VehicleManager, RentalCalculator)")] }),
                new Paragraph({ children: [new TextRun("com.rental.ui: 用户界面 (Main)")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "4. 类设计" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "4.1 类图" })] }),
                new Paragraph({
                    children: [new ImageRun({
                        data: fs.readFileSync(path.join(__dirname, '..', 'diagrams', 'class_diagram.png')),
                        transformation: { width: 450, height: 300 },
                        type: "png"
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "4.2 核心类" })] }),
                new Paragraph({ children: [new TextRun("Main: 系统入口、菜单显示、用户交互")] }),
                new Paragraph({ children: [new TextRun("Vehicle: 封装车辆属性 (id, type, model, dailyRent, status)")] }),
                new Paragraph({ children: [new TextRun("VehicleManager: 车辆列表管理、初始化、查询")] }),
                new Paragraph({ children: [new TextRun("RentalCalculator: 费用计算、折扣率获取")] }),
                new Paragraph({ children: [new TextRun("DiscountStrategy: 折扣策略接口")] }),
                new Paragraph({ children: [new TextRun("DiscountStrategyFactory: 策略工厂")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "5. 设计模式应用" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "5.1 策略模式" })] }),
                new Paragraph({ children: [new TextRun("目的: 封装不同车型的折扣算法，使它们可以互相替换")] }),
                new Paragraph({ children: [new TextRun("实现: DiscountStrategy接口 + Car/Bus/Truck/DefaultDiscountStrategy")] }),
                new Paragraph({
                    children: [new ImageRun({
                        data: fs.readFileSync(path.join(__dirname, '..', 'diagrams', 'discount_strategy_flow.png')),
                        transformation: { width: 350, height: 200 },
                        type: "png"
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "5.2 工厂模式" })] }),
                new Paragraph({ children: [new TextRun("目的: 封装策略对象的创建过程")] }),
                new Paragraph({ children: [new TextRun("实现: DiscountStrategyFactory根据车型返回对应策略")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "6. 流程设计" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "6.1 费用计算流程" })] }),
                new Paragraph({
                    children: [new ImageRun({
                        data: fs.readFileSync(path.join(__dirname, '..', 'diagrams', 'fee_calculation_flow.png')),
                        transformation: { width: 400, height: 200 },
                        type: "png"
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "6.2 租车流程顺序图" })] }),
                new Paragraph({
                    children: [new ImageRun({
                        data: fs.readFileSync(path.join(__dirname, '..', 'diagrams', 'rental_process_sequence.png')),
                        transformation: { width: 450, height: 300 },
                        type: "png"
                    })]
                }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "7. 数据设计" })] }),
                new Paragraph({ children: [new TextRun("Vehicle: id(String), type(String), model(String), dailyRent(double), status(String)")] }),
                new Paragraph({ children: [new TextRun("存储: ArrayList<Vehicle>, 程序启动时初始化7辆默认车辆")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "8. 错误处理设计" })] }),
                new Paragraph({ children: [new TextRun("车辆ID不存在: 显示\"车辆不存在，请重新输入\"")] }),
                new Paragraph({ children: [new TextRun("天数为负数/零: 显示\"租赁天数必须大于0\"")] }),
                new Paragraph({ children: [new TextRun("天数为非数字: 显示\"输入错误，请输入数字\"")] }),
                new Paragraph({ children: [new TextRun("未选择车辆直接计算: 显示\"请先选择车辆并输入租赁天数\"")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "9. 扩展性设计" })] }),
                new Paragraph({ children: [new TextRun("新增车型步骤: 1.创建折扣策略类 2.在工厂添加case 3.(可选)在VehicleManager添加车辆")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "10. 部署与运行" })] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "10.1 环境要求" })] }),
                new Paragraph({ children: [new TextRun("JDK 8+")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: "10.2 编译步骤" })] }),
                new Paragraph({ children: [new TextRun("javac -d bin src/com/rental/**/*.java")]}),
                new Paragraph({ children: [new TextRun("java -cp bin com.rental.ui.Main")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: "11. 总结" })] }),
                new Paragraph({ children: [new TextRun("本系统采用三层架构+策略模式+工厂模式，模块化程度高，易于理解和维护，满足当前功能需求，并具备良好的扩展性。")] }),
            ]
        }]
    });
    return doc;
}

async function generateDocuments() {
    try {
        const requirementsDoc = createRequirementsDoc();
        const requirementsBuffer = await Packer.toBuffer(requirementsDoc);
        fs.writeFileSync(path.join(__dirname, '..', 'docs', '需求规格说明书.docx'), requirementsBuffer);
        console.log("需求规格说明书已生成: docs/需求规格说明书.docx");

        const architectureDoc = createArchitectureDoc();
        const architectureBuffer = await Packer.toBuffer(architectureDoc);
        fs.writeFileSync(path.join(__dirname, '..', 'docs', '架构设计说明书.docx'), architectureBuffer);
        console.log("架构设计说明书已生成: docs/架构设计说明书.docx");
    } catch (error) {
        console.error("生成文档时出错:", error);
    }
}

generateDocuments();
