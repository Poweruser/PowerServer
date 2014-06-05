package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class UnsignedIntPointer {

    private UnsignedInt[] array;
    private int position;

    public UnsignedIntPointer(UnsignedInt[] dest, long i) {
        this.array = dest;
        this.position = (int) i;
    }

    public UnsignedIntPointer createPointerAtOffset(int len) {
        return new UnsignedIntPointer(this.array, this.position + len);
    }

    public boolean greaterThan(UnsignedIntPointer tbuffp) {
        return this.position > tbuffp.position;
    }

    public void movePosition(int i) {
        this.position += i;
    }

    public void setDataAtOffset(int i, UnsignedInt t1) {
        this.array[this.position + i].set(t1);
    }

    public UnsignedInt getDataAtOffset(int i) {
        return this.array[this.position + i];
    }

    public int getPointerPosition() {
        return this.position;
    }

    public UnsignedInt[] getDataArray() {
        return this.array;
    }

}
