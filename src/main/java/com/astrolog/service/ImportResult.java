package com.astrolog.service;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int successCount;
    private final List<String> errors = new ArrayList<>();

    public void addSuccess(int row) {
        successCount++;
    }

    public void addError(int row, String message) {
        errors.add("第" + row + "行: " + message);
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
