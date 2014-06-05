package de.poweruser.powerserver.gamespy.encoders;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.poweruser.powerserver.gamespy.encoders.enctype2.DataSize;
import de.poweruser.powerserver.gamespy.encoders.enctype2.EncType2;
import de.poweruser.powerserver.gamespy.encoders.enctype2.UnsignedChar;
import de.poweruser.powerserver.gamespy.encoders.enctype2.UnsignedCharPointer;

public class EncType2Encoder implements EncoderInterface {

    @Override
    public byte[] encode(String gamekey, String validate, List<InetSocketAddress> servers) throws IOException {
        if(gamekey.length() != 6) { throw new IllegalArgumentException("The game key is not 6 bytes long! >" + gamekey + "<"); }
        if(validate.length() != 8) { throw new IllegalArgumentException("The validate word is not 8 bytes long! >" + validate + "<"); }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteArray);

        List<InetSocketAddress> ipv4 = new ArrayList<InetSocketAddress>();
        Iterator<InetSocketAddress> iter = servers.iterator();
        while(iter.hasNext()) {
            InetSocketAddress i = iter.next();
            InetAddress address = i.getAddress();
            if(address instanceof Inet4Address) {
                ipv4.add(i);
            }
            iter.remove();
        }

        for(InetSocketAddress i: ipv4) {
            this.writeAddress(stream, i);
        }
        stream.writeBytes("\\final\\");

        byte[] data = byteArray.toByteArray();
        int len = data.length;
        stream.close();

        UnsignedChar[] array = new UnsignedChar[len + (1 + 8)];
        for(int i = 0; i < array.length; i++) {
            if(i < data.length) {
                array[i] = new UnsignedChar(data[i]);
            } else {
                array[i] = new UnsignedChar(0);
            }
        }
        UnsignedCharPointer p = new UnsignedCharPointer(array, 0);
        DataSize s = new DataSize(len);
        EncType2 enc = new EncType2();
        int encSize = enc.enctype2_encoder(gamekey, validate, p, s);
        byte[] output = new byte[encSize];
        UnsignedCharPointer z = new UnsignedCharPointer(array, 0);
        for(int i = 0; i < encSize; i++) {
            output[i] = z.getDataAtOffset(i).toSignedByte();
        }
        return output;
    }

    private void writeAddress(DataOutputStream stream, InetSocketAddress address) throws IOException {
        stream.write(address.getAddress().getAddress());
        int port = address.getPort();
        stream.writeByte((port >> 8) & 0xFF);
        stream.writeByte(port & 0xFF);
    }

}
