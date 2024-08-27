package com.example.forexData.util;

public enum Period {
    ONE_WEEK("1W"),
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    NINE_MONTHS("9M"),
    ONE_YEAR("1Y");

    private final String value;

    Period(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Period fromValue(String value) {
        for (Period period : Period.values()) {
            if (period.getValue().equals(value)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid period value: " + value);
    }
}