package com.rental.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON文件存储工具类
 * 提供通用的JSON序列化与反序列化功能
 * 支持对象列表的读写操作
 *
 * @author 系统
 * @version 1.0
 * @param <T> 存储的对象类型
 */
public class JsonFileStorage<T> {
    private static final String DATA_DIR = "data";
    private final Gson gson;
    private final Type typeOfList;
    private final String fileName;

    /**
     * 构造函数
     * @param typeToken 类型令牌，用于泛型类型推断
     * @param fileName 文件名
     */
    public JsonFileStorage(TypeToken<List<T>> typeToken, String fileName) {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.typeOfList = typeToken.getType();
        this.fileName = fileName;
        ensureDataDirExists();
    }

    /**
     * 确保数据目录存在
     */
    private void ensureDataDirExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 获取文件路径
     * @return 文件路径
     */
    private String getFilePath() {
        return DATA_DIR + File.separator + fileName;
    }

    /**
     * 保存对象列表到JSON文件
     * @param list 要保存的对象列表
     */
    public void saveList(List<T> list) {
        try (Writer writer = new FileWriter(getFilePath())) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    /**
     * 从JSON文件加载对象列表
     * @return 对象列表，如果文件不存在返回空列表
     */
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

    /**
     * 检查数据文件是否存在
     * @return true表示文件存在
     */
    public boolean dataFileExists() {
        return new File(getFilePath()).exists();
    }
}