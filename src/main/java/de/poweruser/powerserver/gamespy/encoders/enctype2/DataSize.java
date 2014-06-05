package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class DataSize {

    private int size;

    public DataSize(int size) {
        this.size = size;
    }

    public int toValue() {
        return this.size;
    }

    public void sub(int i) {
        this.size -= i;
    }

    public void set(int i) {
        this.size = i;
    }

    public void add(int i) {
        this.size += i;
    }
}
