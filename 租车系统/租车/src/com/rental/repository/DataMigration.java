
package com.rental.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rental.model.RentalRecord;
import com.rental.model.Vehicle;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 数据迁移工具类
 * 负责将JSON文件数据迁移到MySQL数据库
 * 
 * @author 系统
 * @version 1.0
 */
public class DataMigration {
    
    private static final String VEHICLES_JSON_FILE = "data/vehicles.json";
    private static final String RENTALS_JSON_FILE = "data/rentals.json";
    private static final String VEHICLES_BACKUP_FILE = "data/vehicles_backup.json";
    private static final String RENTALS_BACKUP_FILE = "data/rentals_backup.json";
    
    private final Gson gson;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    
    /**
     * 构造函数
     */
    public DataMigration() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.vehicleRepository = new VehicleRepository();
        this.rentalRepository = new RentalRepository();
    }
    
    /**
     * 执行完整的数据迁移
     * @return 迁移结果信息
     */
    public String migrate() {
        StringBuilder result = new StringBuilder();
        
        try {
            // 1. 备份现有JSON数据
            result.append("1. 备份JSON数据...\n");
            backupJsonData();
            result.append("   ✓ JSON数据备份完成\n\n");
            
            // 2. 迁移车辆数据
            result.append("2. 迁移车辆数据...\n");
            int vehicleCount = migrateVehicles();
            result.append("   ✓ 成功迁移 ").append(vehicleCount).append(" 条车辆记录\n\n");
            
            // 3. 迁移租赁记录数据
            result.append("3. 迁移租赁记录数据...\n");
            int rentalCount = migrateRentalRecords();
            result.append("   ✓ 成功迁移 ").append(rentalCount).append(" 条租赁记录\n\n");
            
            result.append("数据迁移完成！");
            
        } catch (Exception e) {
            result.append("数据迁移失败: ").append(e.getMessage());
            e.printStackTrace();
        }
        
        return result.toString();
    }
    
    /**
     * 迁移车辆数据
     * @return 迁移的记录数
     */
    public int migrateVehicles() {
        List<Vehicle> vehicles = loadVehiclesFromJson();
        int count = 0;
        
        for (Vehicle vehicle : vehicles) {
            // 检查是否已存在
            if (vehicleRepository.findById(vehicle.getId()) == null) {
                vehicleRepository.save(vehicle);
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 迁移租赁记录数据
     * @return 迁移的记录数
     */
    public int migrateRentalRecords() {
        List<RentalRecord> records = loadRentalRecordsFromJson();
        int count = 0;
        
        for (RentalRecord record : records) {
            // 检查是否已存在
            if (!recordExists(record.getId())) {
                rentalRepository.save(record);
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 从JSON文件加载车辆数据
     * @return 车辆列表
     */
    private List<Vehicle> loadVehiclesFromJson() {
        try (FileReader reader = new FileReader(VEHICLES_JSON_FILE)) {
            Type type = new TypeToken<List<Vehicle>>() {}.getType();
            List<Vehicle> vehicles = gson.fromJson(reader, type);
            return vehicles != null ? vehicles : List.of();
        } catch (IOException e) {
            System.err.println("加载车辆JSON文件失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 从JSON文件加载租赁记录数据
     * @return 租赁记录列表
     */
    private List<RentalRecord> loadRentalRecordsFromJson() {
        try (FileReader reader = new FileReader(RENTALS_JSON_FILE)) {
            Type type = new TypeToken<List<RentalRecord>>() {}.getType();
            List<RentalRecord> records = gson.fromJson(reader, type);
            return records != null ? records : List.of();
        } catch (IOException e) {
            System.err.println("加载租赁记录JSON文件失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 检查租赁记录是否已存在
     * @param recordId 记录ID
     * @return true表示已存在
     */
    private boolean recordExists(String recordId) {
        // 通过遍历检查是否存在（因为RentalRepository没有findById方法）
        for (RentalRecord record : rentalRepository.findAll()) {
            if (record.getId().equals(recordId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 备份JSON数据
     */
    private void backupJsonData() {
        backupFile(VEHICLES_JSON_FILE, VEHICLES_BACKUP_FILE);
        backupFile(RENTALS_JSON_FILE, RENTALS_BACKUP_FILE);
    }
    
    /**
     * 备份文件
     * @param source 源文件路径
     * @param destination 目标文件路径
     */
    private void backupFile(String source, String destination) {
        try (FileReader reader = new FileReader(source);
             FileWriter writer = new FileWriter(destination)) {
            
            int c;
            while ((c = reader.read()) != -1) {
                writer.write(c);
            }
            
        } catch (IOException e) {
            System.err.println("备份文件失败 (" + source + "): " + e.getMessage());
        }
    }
    
    /**
     * 检查JSON文件是否存在
     * @return true表示存在
     */
    public boolean jsonFilesExist() {
        return new java.io.File(VEHICLES_JSON_FILE).exists();
    }
    
    /**
     * 检查数据库是否已有数据
     * @return true表示已有数据
     */
    public boolean databaseHasData() {
        return vehicleRepository.dataExists() || rentalRepository.dataExists();
    }
    
    /**
     * 主方法 - 运行数据迁移
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        DataMigration migration = new DataMigration();
        
        // 检查JSON文件是否存在
        if (!migration.jsonFilesExist()) {
            System.out.println("未找到JSON数据文件，跳过数据迁移");
            return;
        }
        
        // 检查数据库是否已有数据
        if (migration.databaseHasData()) {
            System.out.println("数据库中已存在数据，跳过数据迁移");
            System.out.println("如需重新迁移，请先清空数据库");
            return;
        }
        
        // 执行迁移
        String result = migration.migrate();
        System.out.println(result);
    }
}
