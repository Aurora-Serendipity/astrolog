package com.astrolog.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class ValidatorTest {

    @Test
    void testNotNull() {
        ValidationRule rule = Validator.notNull("不能为空");
        assertFalse(rule.validate(null).isValid(), "null 值应校验失败");
        assertTrue(rule.validate("hello").isValid(), "非 null 值应校验通过");
    }

    @Test
    void testMinLength() {
        ValidationRule rule = Validator.minLength(4, "至少4个字符");
        assertFalse(rule.validate("ab").isValid(), "过短值应校验失败");
        assertTrue(rule.validate("abcd").isValid(), "满足长度应校验通过");
    }

    @Test
    void testUsernameValidator() {
        Validator usernameValidator = Validator.usernameValidator();

        assertTrue(usernameValidator.validateFirst("john_doe").isValid(),
            "合法用户名应通过校验");

        List<ValidationResult> results = usernameValidator.validate("ab");
        assertTrue(results.stream().anyMatch(r -> !r.isValid()),
            "过短用户名应校验失败");

        results = usernameValidator.validate("");
        assertTrue(results.stream().anyMatch(r -> !r.isValid()),
            "空用户名应校验失败");
    }

    @Test
    void testPasswordValidator() {
        Validator passwordValidator = Validator.passwordValidator();

        assertTrue(passwordValidator.validateFirst("Abcd1234").isValid(),
            "强密码应通过校验");

        List<ValidationResult> results = passwordValidator.validate("weak");
        assertTrue(results.stream().anyMatch(r -> !r.isValid()),
            "弱密码应校验失败");

        results = passwordValidator.validate("abcdefgh");
        assertTrue(results.stream().anyMatch(r -> !r.isValid()),
            "无大写和数字的密码应校验失败");
    }
}
