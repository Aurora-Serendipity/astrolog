const fs = require('fs');
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, 
        AlignmentType, HeadingLevel, BorderStyle, WidthType, ShadingType, 
        VerticalAlign, LevelFormat, ImageRun, PageBreak } = require('docx');

// 创建需求文档
function createRequirementsDoc() {
    const tableBorder = { style: BorderStyle.SINGLE, size: 1, color: "CCCCCC" };
    const cellBorders = { top: tableBorder, bottom: tableBorder, left: tableBorder, right: tableBorder };

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
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("2. 系统功能模块")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.1 功能模块图")] }),
                new Paragraph({ 
                    alignment: AlignmentType.CENTER,
                    children: [new ImageRun({
                        type: "png",
                        data: fs.readFileSync("diagrams/system_features.png"),
                        transformation: { width: 600, height: 400 },
                        altText: { title: "系统功能模块图", description: "系统功能模块图", name: "系统功能模块图" }
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.2 核心功能")] }),
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
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("3. 业务流程")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("3.1 租车业务流程图")] }),
                new Paragraph({ 
                    alignment: AlignmentType.CENTER,
                    children: [new ImageRun({
                        type: "png",
                        data: fs.readFileSync("diagrams/business_process.png"),
                        transformation: { width: 600, height: 300 },
                        altText: { title: "租车业务流程图", description: "租车业务流程图", name: "租车业务流程图" }
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("3.2 业务流程说明")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("浏览车辆：查看可租赁的车辆列表")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("选择车辆：选择心仪的车辆并确认")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("输入租赁信息：输入租赁天数等信息")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("费用计算：系统根据车型和租赁天数计算费用")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("展示费用：显示详细的费用计算结果")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("4. 价格计算规则")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("4.1 折扣规则图表")] }),
                new Paragraph({ 
                    alignment: AlignmentType.CENTER,
                    children: [new ImageRun({
                        type: "png",
                        data: fs.readFileSync("diagrams/price_calculation_rules.png"),
                        transformation: { width: 700, height: 400 },
                        altText: { title: "价格计算规则图", description: "价格计算规则图", name: "价格计算规则图" }
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("4.2 价格计算规则详情")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("4.2.1 轿车（所有车型）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 7天：享9折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 30天：享8折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 150天：享7折")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("4.2.2 客车（所有车型）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 3天：享9折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 7天：享8折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 30天：享7折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 >= 150天：享6折")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("4.2.3 卡车（所有车型）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 7天：享9.5折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 15天：享8.5折")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("租赁天数 > 30天：享7.5折")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("4.2.4 费用计算公式")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("基础费用 = 日租金 × 租赁天数")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("折扣后费用 = 基础费用 × 折扣率")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("最终费用 = 折扣后费用（四舍五入到整数）")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("5. 系统组件")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("5.1 系统组件图")] }),
                new Paragraph({ 
                    alignment: AlignmentType.CENTER,
                    children: [new ImageRun({
                        type: "png",
                        data: fs.readFileSync("diagrams/system_components.png"),
                        transformation: { width: 600, height: 400 },
                        altText: { title: "系统组件图", description: "系统组件图", name: "系统组件图" }
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("5.2 组件说明")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("核心功能：车辆管理、租车流程、费用计算")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("支持功能：数据存储、用户交互、错误处理")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("6. 命令行界面设计")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.1 界面流程图")] }),
                new Paragraph({ 
                    alignment: AlignmentType.CENTER,
                    children: [new ImageRun({
                        type: "png",
                        data: fs.readFileSync("diagrams/cli_interface_flow.png"),
                        transformation: { width: 600, height: 600 },
                        altText: { title: "命令行界面流程图", description: "命令行界面流程图", name: "命令行界面流程图" }
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.2 主菜单")] }),
                new Paragraph({ children: [new TextRun("租车管理系统")] }),
                new Paragraph({ children: [new TextRun("1. 车辆管理")] }),
                new Paragraph({ children: [new TextRun("2. 租车流程")] }),
                new Paragraph({ children: [new TextRun("3. 退出系统")] }),
                new Paragraph({ children: [new TextRun("请选择操作：")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("6.3 租车流程界面")] }),
                new Paragraph({ children: [new TextRun("租车流程")] }),
                new Paragraph({ children: [new TextRun("1. 查看可租赁车辆")] }),
                new Paragraph({ children: [new TextRun("2. 选择车辆")] }),
                new Paragraph({ children: [new TextRun("3. 输入租赁天数")] }),
                new Paragraph({ children: [new TextRun("4. 计算费用")] }),
                new Paragraph({ children: [new TextRun("5. 返回主菜单")] }),
                new Paragraph({ children: [new TextRun("请选择操作：")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("7. 数据需求")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.1 数据结构")] }),
                new Paragraph({ 
                    alignment: AlignmentType.CENTER,
                    children: [new ImageRun({
                        type: "png",
                        data: fs.readFileSync("diagrams/data_structure.png"),
                        transformation: { width: 500, height: 400 },
                        altText: { title: "数据结构图表", description: "数据结构图表", name: "数据结构图表" }
                    })]
                }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("7.2 车辆数据")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("车辆ID")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("车型（轿车/客车/卡车）")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("具体车型名称")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("日租金")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("车辆状态（可租赁/已租赁）")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("8. 非功能需求")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.1 性能要求")] }),
                new Paragraph({ children: [new TextRun("命令行响应速度快，操作流畅")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.2 可靠性要求")] }),
                new Paragraph({ children: [new TextRun("数据存储安全可靠")] }),
                new Paragraph({ children: [new TextRun("系统稳定性高，无崩溃情况")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("8.3 易用性要求")] }),
                new Paragraph({ children: [new TextRun("命令行界面简洁明了")] }),
                new Paragraph({ children: [new TextRun("操作流程简单直观")] }),
                new Paragraph({ children: [new TextRun("提供清晰的错误提示")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("9. 实现计划")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("9.1 技术选型")] }),
                new Paragraph({ children: [new TextRun("开发语言：Java")] }),
                new Paragraph({ children: [new TextRun("数据存储：内存存储")] }),
                new Paragraph({ children: [new TextRun("界面：命令行界面")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("9.2 模块划分")] }),
                createModuleTable(tableBorder, cellBorders),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("9.3 开发步骤")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("搭建项目结构")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现基础数据结构")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现车辆管理功能")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现折扣策略模式")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现费用计算逻辑")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("实现主菜单和交互逻辑")] }),
                new Paragraph({ numbering: { reference: "numbered-list", level: 0 }, children: [new TextRun("测试和优化")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("10. 测试计划")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("10.1 功能测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试车辆管理功能")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试租车流程")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试费用计算准确性")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("10.2 边界测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试不同租赁天数的折扣计算")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试车辆状态切换")] }),
                new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("10.3 异常测试")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试输入错误数据的处理")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试系统稳定性")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("11. 项目交付")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("完整的命令行租车管理系统")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("系统使用说明文档")] }),
                new Paragraph({ numbering: { reference: "bullet-list", level: 0 }, children: [new TextRun("测试报告")] }),
                
                new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("12. 后续扩展")] }),
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

function createModuleTable(tableBorder, cellBorders) {
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
async function generateDocument() {
    try {
        const requirementsDoc = createRequirementsDoc();
        const requirementsBuffer = await Packer.toBuffer(requirementsDoc);
        fs.writeFileSync("docs/需求文档.docx", requirementsBuffer);
        console.log("需求文档已生成: docs/需求文档.docx");
    } catch (error) {
        console.error("生成文档时出错:", error);
    }
}

generateDocument();
