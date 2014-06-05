package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class EncTypeShared {

    protected static void encshare1(UnsignedInt[] dest, UnsignedCharPointer datap, DataSize size) {
        int len = size.toValue();

        UnsignedIntPointer q = new UnsignedIntPointer(dest, 309);
        EncTypeShared.encshare2(dest, q, 16);

        UnsignedCharToIntPointer p = new UnsignedCharToIntPointer(dest, 309);
        UnsignedCharToIntPointer s = p.copy();

        UnsignedCharPointer datapc = datap.createPointerAtOffset(0);

        while(len-- > 0) {
            int y = p.getDistanceInChar(s);
            if(y == 63) {
                p = s.copy();
                EncTypeShared.encshare2(dest, p.createIntPointer(), 16);
            }
            UnsignedChar x = p.getData();
            datapc.XORAtOffset(0, x);
            datapc.movePosition(1);
            p.movePositionByChar(1);
        }
    }

    protected static void encshare2(UnsignedInt[] tbuff, UnsignedIntPointer tbuffq, int len) {
        UnsignedInt t2 = tbuff[304].copy();
        UnsignedInt t1 = tbuff[305].copy();
        UnsignedInt t3 = tbuff[306].copy();
        UnsignedInt t5 = tbuff[307].copy();
        UnsignedInt t4 = new UnsignedInt(0);

        UnsignedIntPointer tbuffp = tbuffq.createPointerAtOffset(0);
        UnsignedIntPointer limit = tbuffp.createPointerAtOffset(len);
        UnsignedIntPointer p;

        while(limit.greaterThan(tbuffp)) {
            p = new UnsignedIntPointer(tbuff, t2.toLong() + 272L);
            while(t5.toLong() < 65535L) {
                t1.add(t5);
                p.movePosition(1);
                t3.add(t1);
                t1.add(t3);
                p.setDataAtOffset(-17, t1);
                p.setDataAtOffset(-1, t3);
                t4 = t3.createRotationRight(8);
                p.setDataAtOffset(15, t5);
                t5.shiftLeft(1);
                t2.add(new UnsignedInt(1));
                t1.XOR(tbuff[t1.toByteValue()]);
                t4.XOR(tbuff[t4.toByteValue()]);
                t3 = t4.createRotationRight(8);
                t4 = t1.createRotationLeft(8);
                t4.XOR(tbuff[t4.toByteValue()]);
                t3.XOR(tbuff[t3.toByteValue()]);
                t1 = t4.createRotationLeft(8);
            }

            t3.XOR(t1);
            UnsignedInt x = tbuffp.getDataAtOffset(0);
            x.set(t3);
            tbuffp.movePosition(1);
            t2.sub(new UnsignedInt(1));
            t1.set(tbuff[t2.toInt() + 256]);
            t5.set(tbuff[t2.toInt() + 272]);
            t1.complement();
            t3 = t1.createRotationRight(8);
            t3.XOR(tbuff[t3.toByteValue()]);
            t5.XOR(tbuff[t5.toByteValue()]);
            t1 = t3.createRotationRight(8);
            t4 = t5.createRotationLeft(8);
            t1.XOR(tbuff[t1.toByteValue()]);
            t4.XOR(tbuff[t4.toByteValue()]);
            t3 = t4.createRotationLeft(8);
            UnsignedInt x2 = tbuff[t2.toInt() + 288].createShiftLeft(1);
            x2.add(new UnsignedInt(1));
            t5.set(x2);
        }
        tbuff[304].set(t2);
        tbuff[305].set(t1);
        tbuff[306].set(t3);
        tbuff[307].set(t5);
    }

    protected static void encshare3(UnsignedInt[] data, int n1, int n2) {
        UnsignedInt t2 = new UnsignedInt(n1);
        UnsignedInt t1 = new UnsignedInt(0);
        UnsignedInt t3 = new UnsignedInt(0);
        UnsignedInt t4 = new UnsignedInt(1);
        data[304].set(new UnsignedInt(0));

        for(int i = 32768; i > 0; i >>= 1) {

            t2.add(t4);
            t1.add(t2);
            t2.add(t1);

            if((n2 & i) != 0) {
                t2.complement();
                t4.shiftLeft(1);
                t4.add(new UnsignedInt(1));
                t3.set(t2.createRotationRight(8));
                t3.XOR(data[t3.toByteValue()]);
                t1.XOR(data[t1.toByteValue()]);
                t2.set(t3.createRotationRight(8));
                t3.set(t1.createRotationLeft(8));
                t2.XOR(data[t2.toByteValue()]);
                t3.XOR(data[t3.toByteValue()]);
                t1.set(t3.createRotationLeft(8));
            } else {
                data[data[304].toInt() + 256].set(t2);
                data[data[304].toInt() + 272].set(t1);
                data[data[304].toInt() + 288].set(t4);
                data[304].add(new UnsignedInt(1));
                t3.set(t1.createRotationRight(8));
                t2.XOR(data[t2.toByteValue()]);
                t3.XOR(data[t3.toByteValue()]);
                t1.set(t3.createRotationRight(8));
                t3.set(t2.createRotationLeft(8));
                t3.XOR(data[t3.toByteValue()]);
                t1.XOR(data[t1.toByteValue()]);
                t2.set(t3.createRotationLeft(8));
                t4.shiftLeft(1);
            }
        }

        data[305].set(t2);
        data[306].set(t1);
        data[307].set(t4);
        data[308].set(new UnsignedInt(n1));
    }

    protected static void encshare4(UnsignedCharPointer src, int size, UnsignedInt[] dest) {
        for(int i = 0; i < 256; i++) {
            dest[i].set(new UnsignedInt(0));
        }

        UnsignedInt tmp;

        for(int y = 0; y < 4; y++) {
            for(int i = 0; i < 256; i++) {
                dest[i].shiftLeft(8);
                dest[i].add(new UnsignedInt(i));
            }

            for(int pos = y, x = 0; x < 2; x++) {
                for(int j = 0; j < 256; j++) {
                    tmp = dest[j].copy();
                    pos += tmp.toLong() + src.getDataAtOffset(j % size).toInt();
                    pos &= 0xFF;
                    dest[j].set(dest[pos]);
                    dest[pos].set(tmp);
                }
            }
        }

        for(int i = 0; i < 256; i++) {
            dest[i].XOR(new UnsignedInt(i));
        }

        EncTypeShared.encshare3(dest, 0, 0);
    }
}
