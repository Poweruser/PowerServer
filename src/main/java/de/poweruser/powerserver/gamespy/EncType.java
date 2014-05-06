package de.poweruser.powerserver.gamespy;

public enum EncType {
    BASIC(0),
    ADVANCED1(1),
    ADVANCED2(2),
    NONE(3);

    private int type;

    private EncType(int type) {
        this.type = type;
    }

    public int getTypeValue() {
        return this.type;
    }

    public static EncType getTypeFromValue(int value) {
        switch(value) {
            case 0:
                return BASIC;
            case 1:
                return ADVANCED1;
            case 2:
                return ADVANCED2;
            case 3:
                return NONE;
            default:
        }
        return null;
    }
}
