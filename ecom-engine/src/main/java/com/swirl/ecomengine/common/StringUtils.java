package com.swirl.ecomengine.common;

public class StringUtils {

    // ---------------------------------------------------------
    // CHECK: NOT NULL AND NOT BLANK
    // ---------------------------------------------------------
    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}