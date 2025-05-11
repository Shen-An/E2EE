package com.easyChat.enums;

public enum DateTimePatternEnum {
    YYYY_MM_DD_HH_MM_SS("yyyy_MM_dd HH:mm:ss"),YYYY_MM_DD("yyyy-MM-dd"),YYYYMM("yyyyMM"),;
    private String pattern;

    DateTimePatternEnum(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
