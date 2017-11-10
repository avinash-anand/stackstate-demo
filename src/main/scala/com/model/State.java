package com.model;

public enum State {
    NO_DATA("no_data"), CLEAR("clear"), WARNING("warning"), ALERT("alert");

    private final String value;

    State(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static State toEnum(String s) {
        switch (s) {
            case "no_data":
                return NO_DATA;
            case "clear":
                return CLEAR;
            case "warning":
                return WARNING;
            case "alert":
                return ALERT;
            default:
                throw new RuntimeException("Unknown Value for Enum - State");
        }
    }

}
