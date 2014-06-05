package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class UnsignedCharToIntPointer {

    private UnsignedInt[] data;
    private int absolutePosition;

    public UnsignedCharToIntPointer(UnsignedInt[] array, int offset) {
        this.data = array;
        this.absolutePosition = 0;
        this.movePositionByInt(offset);
    }

    private void movePositionByInt(int diff) {
        this.absolutePosition += (4 * diff);
    }

    public void movePositionByChar(int diff) {
        this.absolutePosition += diff;
    }

    public int getDistanceInChar(UnsignedCharToIntPointer ui) {
        return this.absolutePosition - ui.absolutePosition;
    }

    public UnsignedCharToIntPointer copy() {
        UnsignedCharToIntPointer out = new UnsignedCharToIntPointer(this.data, 0);
        out.absolutePosition = this.absolutePosition;
        return out;
    }

    public UnsignedIntPointer createIntPointer() {
        if(this.toCharPosition() == 0) { return new UnsignedIntPointer(this.data, this.toIntPosition()); }
        return null;
    }

    private int toIntPosition() {
        return this.absolutePosition / 4;
    }

    private int toCharPosition() {
        return this.absolutePosition % 4;
    }

    public UnsignedChar getData() {
        UnsignedInt i = this.data[this.toIntPosition()];
        switch(this.toCharPosition()) {
            default:
            case 0:
                return i.getFirstByteOfValue();
            case 1:
                return i.getSecondByteOfValue();
            case 2:
                return i.getThirdByteOfValue();
            case 3:
                return i.getForthByteOfValue();
        }
    }

    public int getPointerPositionInInt() {
        if(this.toCharPosition() == 0) { return this.toIntPosition(); }
        return -1;
    }

    public UnsignedInt[] getDataArray() {
        return this.data;
    }
}
