package de.poweruser.powerserver.gamespy;

public enum EncType {
    BASIC(0),
    ADVANCED1(1),
    ADVANCED2(2);

    private int type;

    private EncType(int type) {
        this.type = type;
    }

    public int getTypeValue() {
        return this.type;
    }
}
