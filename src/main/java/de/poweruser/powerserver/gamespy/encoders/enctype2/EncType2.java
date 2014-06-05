package de.poweruser.powerserver.gamespy.encoders.enctype2;

public class EncType2 {

    public int enctype2_wrapper(String key, UnsignedCharPointer data, int size) {
        UnsignedCharPointer p;
        DataSize s = new DataSize(size);
        p = enctype2_decoder(key, data, s);
        if(p.greaterThan(data)) {
            for(int i = 0; i < size; i++) {
                data.setDataAtOffset(i, p.getDataAtOffset(i));
            }
        }
        return s.toValue();
    }

    public UnsignedCharPointer enctype2_decoder(String key, UnsignedCharPointer data, DataSize s) {
        UnsignedInt[] dest = new UnsignedInt[326];

        data.XORAtOffset(0, new UnsignedChar(0xEC));
        UnsignedCharPointer datap = data.createPointerAtOffset(1);

        byte[] bkey = key.getBytes();
        for(int i = 0; i < key.length(); i++) {
            datap.XORAtOffset(i, new UnsignedChar(bkey[i]));
        }

        for(int i = 0; i < 326; i++) {
            dest[i] = new UnsignedInt(0);
        }
        if(!data.getDataAtOffset(0).sameValueAs(new UnsignedChar(0))) {
            EncTypeShared.encshare4(datap.createPointerAtOffset(0), data.getDataAtOffset(0).toInt(), dest);
        }
        int diff = data.getDataAtOffset(0).toInt();
        datap.movePosition(diff);

        s.sub(diff + 1);
        if(s.toValue() < 6) {
            s.set(0);
            return data;
        }

        EncTypeShared.encshare1(dest, datap.createPointerAtOffset(0), s);

        s.sub(6);
        return datap;
    }

    public int enctype2_encoder(String gamekey, String validate, UnsignedCharPointer data, DataSize s) {
        int header_size = 8;
        UnsignedInt[] dest = new UnsignedInt[326];
        for(int i = 0; i < dest.length; i++) {
            dest[i] = new UnsignedInt(0);
        }
        for(int i = s.toValue() - 1; i >= 0; i--) {
            data.getDataAtOffset(1 + header_size + i).set(data.getDataAtOffset(i));
        }
        data.setDataAtOffset(0, new UnsignedChar(header_size));
        UnsignedCharPointer datap = data.createPointerAtOffset(1);
        byte[] valBytes = validate.getBytes();
        for(int i = 0; i < header_size; i++) {
            datap.setDataAtOffset(i, new UnsignedChar(valBytes[i] & 0xFF));
        }
        for(int i = 256; i < 326; i++) {
            dest[i] = new UnsignedInt(0);
        }
        EncTypeShared.encshare4(datap.createPointerAtOffset(0), data.getDataAtOffset(0).toInt(), dest);

        EncTypeShared.encshare1(dest, datap.createPointerAtOffset(data.getDataAtOffset(0).toInt()), new DataSize(s.toValue()));

        byte[] keybytes = gamekey.getBytes();
        for(int i = 0; i < gamekey.length(); i++) {
            datap.XORAtOffset(i, new UnsignedChar(keybytes[i] & 0xFF));
        }
        s.add(1 + data.getDataAtOffset(0).toInt());
        data.XORAtOffset(0, new UnsignedChar(0xEC));
        return s.toValue();
    }
}
