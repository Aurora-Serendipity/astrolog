package com.astrolog.util;

@FunctionalInterface
public interface ValidationRule {
    ValidationResult validate(Object value);
}
