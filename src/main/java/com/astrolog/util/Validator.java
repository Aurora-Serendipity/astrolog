package com.astrolog.util;

import java.util.ArrayList;
import java.util.List;

public class Validator {
    private final List<ValidationRule> rules = new ArrayList<>();

    public Validator addRule(ValidationRule rule) {
        rules.add(rule);
        return this;
    }

    public List<ValidationResult> validate(Object value) {
        List<ValidationResult> results = new ArrayList<>();
        for (ValidationRule rule : rules) {
            results.add(rule.validate(value));
        }
        return results;
    }

    public ValidationResult validateFirst(Object value) {
        for (ValidationRule rule : rules) {
            ValidationResult result = rule.validate(value);
            if (!result.isValid()) {
                return result;
            }
        }
        return ValidationResult.success();
    }

    public static ValidationRule notNull(String message) {
        return value -> value != null ? ValidationResult.success() : ValidationResult.fail(message);
    }

    public static ValidationRule notBlank(String message) {
        return value -> {
            if (value == null) return ValidationResult.fail(message);
            if (value instanceof String s && s.isBlank()) return ValidationResult.fail(message);
            return ValidationResult.success();
        };
    }

    public static ValidationRule minLength(int min, String message) {
        return value -> {
            if (value instanceof String s && s.length() >= min) return ValidationResult.success();
            return ValidationResult.fail(message);
        };
    }

    public static ValidationRule maxLength(int max, String message) {
        return value -> {
            if (value instanceof String s && s.length() <= max) return ValidationResult.success();
            return ValidationResult.fail(message);
        };
    }

    public static ValidationRule pattern(String regex, String message) {
        return value -> {
            if (value instanceof String s && s.matches(regex)) return ValidationResult.success();
            return ValidationResult.fail(message);
        };
    }

    public static ValidationRule range(double min, double max, String message) {
        return value -> {
            if (value instanceof Number n) {
                double d = n.doubleValue();
                if (d >= min && d <= max) return ValidationResult.success();
            }
            return ValidationResult.fail(message);
        };
    }

    public static Validator usernameValidator() {
        return new Validator()
            .addRule(notNull("用户名不能为空"))
            .addRule(notBlank("用户名不能为空"))
            .addRule(minLength(4, "用户名至少4个字符"))
            .addRule(maxLength(20, "用户名最多20个字符"))
            .addRule(pattern("^[a-zA-Z0-9_]+$", "用户名只能包含字母、数字和下划线"));
    }

    public static Validator passwordValidator() {
        return new Validator()
            .addRule(notNull("密码不能为空"))
            .addRule(minLength(8, "密码至少8个字符"))
            .addRule(pattern(".*[A-Z].*", "密码必须包含大写字母"))
            .addRule(pattern(".*[a-z].*", "密码必须包含小写字母"))
            .addRule(pattern(".*\\d.*", "密码必须包含数字"));
    }

    public static Validator latitudeValidator() {
        return new Validator()
            .addRule(notNull("纬度不能为空"))
            .addRule(range(-90, 90, "纬度必须在-90到90之间"));
    }

    public static Validator longitudeValidator() {
        return new Validator()
            .addRule(notNull("经度不能为空"))
            .addRule(range(-180, 180, "经度必须在-180到180之间"));
    }
}
