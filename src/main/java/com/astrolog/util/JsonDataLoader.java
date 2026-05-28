package com.astrolog.util;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsonDataLoader {
    private static final Gson gson = new Gson();

    private JsonDataLoader() {}

    public static <T> List<T> loadList(String resourcePath, Class<T[]> clazz) {
        try (Reader reader = new InputStreamReader(
                JsonDataLoader.class.getClassLoader().getResourceAsStream(resourcePath),
                StandardCharsets.UTF_8)) {
            if (reader == null) {
                System.err.println("Resource not found: " + resourcePath);
                return Collections.emptyList();
            }
            T[] array = gson.fromJson(reader, clazz);
            return Arrays.asList(array);
        } catch (Exception e) {
            System.err.println("Failed to load JSON resource: " + resourcePath + " - " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static <T> T loadObject(String resourcePath, Class<T> clazz) {
        try (Reader reader = new InputStreamReader(
                JsonDataLoader.class.getClassLoader().getResourceAsStream(resourcePath),
                StandardCharsets.UTF_8)) {
            if (reader == null) {
                System.err.println("Resource not found: " + resourcePath);
                return null;
            }
            return gson.fromJson(reader, clazz);
        } catch (Exception e) {
            System.err.println("Failed to load JSON resource: " + resourcePath + " - " + e.getMessage());
            return null;
        }
    }
}
