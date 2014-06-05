package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class UnsignedCharPointer {

    private UnsignedChar[] array;
    private int position;

    public UnsignedCharPointer(UnsignedChar[] array, int offset) {
        this.array = array;
        this.position = offset;
    }

    public boolean greaterThan(UnsignedCharPointer data) {
        return this.position > data.position;
    }

    public UnsignedChar getDataAtOffset(int i) {
        return this.array[this.position + i];
    }

    public void setDataAtOffset(int i, UnsignedChar dataAtOffset) {
        this.setData(this.position + i, dataAtOffset);
    }

    public void XORAtOffset(int i, UnsignedChar j) {
        UnsignedChar u = this.getData(this.position + i);
        u.XOR(j);
    }

    public UnsignedCharPointer createPointerAtOffset(int i) {
        return new UnsignedCharPointer(this.array, this.position + i);
    }

    public void movePosition(int diff) {
        this.position += diff;
    }

    private UnsignedChar getData(int position) {
        return this.array[position];
    }

    private void setData(int position, UnsignedChar ui) {
        this.array[position].set(ui);
    }

    public int getPointerPosition() {
        return this.position;
    }

    public UnsignedChar[] getDataArray() {
        return this.array;
    }
}
