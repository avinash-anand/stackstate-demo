package com.model;

public enum CheckState {

    CPU_LOAD("CPU load"), RAM_USAGE("RAM usage");

    private final String value;

    CheckState(String value) {
        this.value = value;
    }

    public static CheckState toEnum(String s) {
        switch (s) {
            case "CPU load":
                return CPU_LOAD;
            case "RAM usage":
                return RAM_USAGE;
            default:
                throw new RuntimeException("Unknown value for enum - CheckState");
        }
    }

    public String getValue() {
        return this.value;
    }

}
