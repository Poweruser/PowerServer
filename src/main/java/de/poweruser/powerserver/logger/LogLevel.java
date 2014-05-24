package de.poweruser.powerserver.logger;

public enum LogLevel {

    VERY_LOW(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    VERY_HIGH(4);

    private int value;

    private LogLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static LogLevel getMaxLevel() {
        return VERY_HIGH;
    }

    public static LogLevel valueToLevel(int value) {
        for(LogLevel l: values()) {
            if(l.getValue() == value) { return l; }
        }
        return null;
    }

    public boolean doesPass(LogLevel filter) {
        return this.getValue() <= filter.getValue();
    }
}
