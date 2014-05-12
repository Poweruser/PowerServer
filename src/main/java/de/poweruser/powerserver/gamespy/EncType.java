package de.poweruser.powerserver.gamespy;

import de.poweruser.powerserver.gamespy.encoders.EncoderInterface;
import de.poweruser.powerserver.gamespy.encoders.OFPMonitorEncoder;

public enum EncType {
    BASIC(0, null),
    ADVANCED1(1, null),
    ADVANCED2(2, null),
    OFPMONITOR(3, new OFPMonitorEncoder());

    private int type;
    private EncoderInterface encoder;

    private EncType(int type, EncoderInterface encoder) {
        this.type = type;
        this.encoder = encoder;
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
                return OFPMONITOR;
            default:
        }
        return null;
    }

    public EncoderInterface getEncoder() {
        return this.encoder;
    }
}
