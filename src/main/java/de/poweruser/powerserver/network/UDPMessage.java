package de.poweruser.powerserver.network;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class UDPMessage {

    private final byte[] data;
    private final InetSocketAddress sender;

    public UDPMessage(DatagramPacket packet) {
        int length = packet.getLength();
        this.data = new byte[length];
        System.arraycopy(packet.getData(), 0, this.data, 0, length);
        this.sender = new InetSocketAddress(packet.getAddress(), packet.getPort());
    }

    public final InetSocketAddress getSender() {
        return this.sender;
    }

    public final byte[] getData() {
        return this.data;
    }

    @Override
    public String toString() {
        try {
            return new String(data, 0, data.length, "UTF-8");
        } catch(UnsupportedEncodingException thr) {
            return new String(data, 0, data.length);
        }
    }
}
