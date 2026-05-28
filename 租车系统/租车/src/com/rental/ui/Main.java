package com.rental.ui;

import com.rental.model.Vehicle;
import com.rental.service.RentalCalculator;
import com.rental.service.VehicleManager;
import java.util.Scanner;

/**
 * 命令行界面入口类
 * 提供基于命令行的租车管理系统交互界面
 * 支持车辆查看、租车流程等基本功能
 *
 * @author 系统
 * @version 1.0
 */
public class Main {
    /** 标准输入扫描器，用于读取用户输入 */
    private static Scanner scanner = new Scanner(System.in);

    /** 车辆管理器实例 */
    private static VehicleManager vehicleManager = new VehicleManager();

    /** 当前选中的车辆 */
    private static Vehicle selectedVehicle = null;

    /** 当前设置的租赁天数 */
    private static int rentalDays = 0;

    /**
     * 程序主入口
     * 初始化车辆数据并进入主循环
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("欢迎使用租车管理系统");
        // 初始化车辆数据（从文件加载或创建默认数据）
        vehicleManager.initializeVehicles();

        // 主循环，持续显示菜单并处理用户选择
        while (true) {
            showMainMenu();
            String choice = scanner.nextLine();
            handleMainMenuChoice(choice);
        }
    }

    /**
     * 显示主菜单
     * 提供车辆管理、租车流程和退出三个选项
     */
    private static void showMainMenu() {
        System.out.println("\n租车管理系统");
        System.out.println("1. 车辆管理");
        System.out.println("2. 租车流程");
        System.out.println("3. 退出系统");
        System.out.print("请选择操作：");
    }

    /**
     * 处理主菜单选择
     * 根据用户输入执行相应操作
     *
     * @param choice 用户选择的菜单项
     */
    private static void handleMainMenuChoice(String choice) {
        switch (choice) {
            case "1":
                // 车辆管理功能
                manageVehicles();
                break;
            case "2":
                // 租车流程
                rentalProcess();
                break;
            case "3":
                // 退出系统
                System.out.println("感谢使用，再见！");
                System.exit(0);
                break;
            default:
                // 输入错误提示
                System.out.println("输入错误，请重新选择");
                break;
        }
    }

    /**
     * 车辆管理功能
     * 显示所有可租赁车辆的列表
     */
    private static void manageVehicles() {
        System.out.println("\n可租赁车辆列表：");
        for (Vehicle vehicle : vehicleManager.getAvailableVehicles()) {
            System.out.printf("ID: %s, 车型: %s, 具体车型: %s, 日租金: ¥%.2f\n",
                vehicle.getId(), vehicle.getType(), vehicle.getModel(), vehicle.getDailyRent());
        }
    }

    /**
     * 租车流程主循环
     * 持续显示租车子菜单直到用户返回主菜单
     */
    private static void rentalProcess() {
        while (true) {
            showRentalMenu();
            String choice = scanner.nextLine();
            // 返回主菜单时返回true
            if (handleRentalMenuChoice(choice)) {
                break;
            }
        }
    }

    /**
     * 显示租车子菜单
     */
    private static void showRentalMenu() {
        System.out.println("\n租车流程");
        System.out.println("1. 查看可租赁车辆");
        System.out.println("2. 选择车辆");
        System.out.println("3. 输入租赁天数");
        System.out.println("4. 计算费用");
        System.out.println("5. 返回主菜单");
        System.out.print("请选择操作：");
    }

    /**
     * 处理租车子菜单选择
     *
     * @param choice 用户选择的菜单项
     * @return true表示返回主菜单，false表示继续租车流程
     */
    private static boolean handleRentalMenuChoice(String choice) {
        switch (choice) {
            case "1":
                // 查看可租赁车辆
                manageVehicles();
                break;
            case "2":
                // 选择车辆
                System.out.print("请输入车辆ID：");
                String vehicleId = scanner.nextLine();
                selectedVehicle = vehicleManager.getVehicleById(vehicleId);
                if (selectedVehicle != null) {
                    System.out.printf("已选择车辆：%s %s，日租金：¥%.2f\n",
                        selectedVehicle.getType(), selectedVehicle.getModel(), selectedVehicle.getDailyRent());
                } else {
                    System.out.println("车辆不存在，请重新输入");
                }
                break;
            case "3":
                // 输入租赁天数
                System.out.print("请输入租赁天数：");
                try {
                    rentalDays = Integer.parseInt(scanner.nextLine());
                    if (rentalDays <= 0) {
                        System.out.println("租赁天数必须大于0");
                    } else {
                        System.out.println("已设置租赁天数：" + rentalDays + "天");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("输入错误，请输入数字");
                }
                break;
            case "4":
                // 计算费用
                if (selectedVehicle == null || rentalDays <= 0) {
                    System.out.println("请先选择车辆并输入租赁天数");
                } else {
                    // 计算基础费用
                    double baseFee = selectedVehicle.getDailyRent() * rentalDays;
                    // 获取折扣率
                    double discountRate = RentalCalculator.getDiscountRate(selectedVehicle.getType(), rentalDays);
                    // 计算折后费用
                    double discountedFee = baseFee * discountRate;
                    // 四舍五入得到最终费用
                    int finalFee = (int) Math.round(discountedFee);

                    System.out.println("\n车辆信息：" + selectedVehicle.getType() + " " + selectedVehicle.getModel());
                    System.out.printf("日租金：¥%.2f\n", selectedVehicle.getDailyRent());
                    System.out.println("租赁天数：" + rentalDays + "天");
                    System.out.printf("基础费用：¥%.2f\n", baseFee);
                    System.out.printf("折扣率：%.0f%%\n", discountRate * 100);
                    System.out.printf("最终费用：¥%d\n", finalFee);
                }
                break;
            case "5":
                // 返回主菜单
                return true;
            default:
                System.out.println("输入错误，请重新选择");
                break;
        }
        return false;
    }
}