package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class UnsignedChar {

    private short value;
    public static final short MAX = 0xFF;

    public UnsignedChar(int i) {
        this.value = (short) (i & MAX);
    }

    public int toInt() {
        return this.value;
    }

    public byte toSignedByte() {
        return (byte) this.value;
    }

    public void set(UnsignedChar dataAtOffset) {
        this.value = dataAtOffset.value;
    }

    public void XOR(UnsignedChar j) {
        this.value = (short) ((this.value ^ j.value) & MAX);
    }

    public boolean sameValueAs(UnsignedChar ui) {
        return this.toInt() == ui.toInt();
    }
}
