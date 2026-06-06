package com.classroom.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Service
public class InputSecurityService {
    private static final int MAX_SEARCH_LENGTH = 100;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_.-]{1,50}");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(--|/\\*|\\*/|;|\\bunion\\s+select\\b|\\bselect\\s+.+\\s+from\\b|"
                    + "\\binsert\\s+into\\b|\\bupdate\\s+.+\\s+set\\b|\\bdelete\\s+from\\b|"
                    + "\\bdrop\\s+(table|database)\\b|\\bexec(ute)?\\b|\\bxp_cmdshell\\b|"
                    + "\\bor\\s+['\"]?\\d+['\"]?\\s*=\\s*['\"]?\\d+['\"]?)",
            Pattern.CASE_INSENSITIVE);

    public String validateUsername(String username) {
        String value = required(username, "账号");
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("账号只能包含字母、数字、点、下划线和短横线");
        }
        return value;
    }

    public String validateSearch(String input, String fieldName) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        String value = input.trim();
        if (value.length() > MAX_SEARCH_LENGTH) {
            throw new IllegalArgumentException(fieldName + "不能超过 " + MAX_SEARCH_LENGTH + " 个字符");
        }
        rejectSqlFragments(value);
        return value;
    }

    public String validateDate(String input) {
        String value = required(input, "日期");
        try {
            LocalDate.parse(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("日期格式必须为 YYYY-MM-DD");
        }
        return value;
    }

    private String required(String input, String fieldName) {
        if (!StringUtils.hasText(input)) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        String value = input.trim();
        rejectSqlFragments(value);
        return value;
    }

    private void rejectSqlFragments(String input) {
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            throw new IllegalArgumentException("输入包含不允许的 SQL 特征");
        }
    }
}
