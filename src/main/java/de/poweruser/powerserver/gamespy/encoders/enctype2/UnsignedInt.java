package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class UnsignedInt {

    private long value;
    private static final long MAX = 0xFFFFFFFFL;

    public UnsignedInt(long n1) {
        this.set(n1);
    }

    public UnsignedInt(int n1) {
        this.set(n1);
    }

    public void add(UnsignedInt t2) {
        this.set(this.value + t2.value);
    }

    public void sub(UnsignedInt t2) {
        this.set(this.value - t2.value);
    }

    public void complement() {
        this.set(~this.value);

    }

    public void shiftLeft(int i) {
        this.set(this.value << i);
    }

    private void set(long l) {
        this.value = (l & MAX);
    }

    public void set(UnsignedInt ui) {
        this.set(ui.value);
    }

    public UnsignedInt createRotationRight(int range) {
        long fullRange = (long) (Math.pow(2, range) - 1L);
        long split = this.value & fullRange;
        long out = (this.value >> range) | (split << (32 - range));
        return new UnsignedInt(out);
    }

    public UnsignedInt createRotationLeft(int range) {
        long upperBits = 0xFFFFFFFF00000000L;
        long leftshift = this.value << range;
        long out = (leftshift & MAX) | ((leftshift & upperBits) >> 32);
        return new UnsignedInt(out);
    }

    public int toInt() {
        return (int) this.value;
    }

    public long toLong() {
        return this.value;
    }

    public void XOR(UnsignedInt unsignedInt) {
        this.set(this.value ^ unsignedInt.value);

    }

    public int toByteValue() {
        return (int) (this.value & 0xFFL);
    }

    public UnsignedInt createShiftLeft(int i) {
        return new UnsignedInt(this.value << i);
    }

    public UnsignedInt copy() {
        return new UnsignedInt(this.value);
    }

    public UnsignedChar getFirstByteOfValue() {
        return new UnsignedChar((int) (this.value & 0xFFL));
    }

    public UnsignedChar getSecondByteOfValue() {
        return new UnsignedChar((int) ((this.value >> 8) & 0xFFL));
    }

    public UnsignedChar getThirdByteOfValue() {
        return new UnsignedChar((int) ((this.value >> 16) & 0xFFL));
    }

    public UnsignedChar getForthByteOfValue() {
        return new UnsignedChar((int) ((this.value >> 24) & 0xFFL));
    }
}
