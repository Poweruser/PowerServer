package de.poweruser.powerserver.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPMessage {

    private final byte[] data;
    private final InetAddress sender;

    public UDPMessage(DatagramPacket packet) {
        int length = packet.getLength();
        this.data = new byte[length];
        System.arraycopy(packet.getData(), 0, this.data, 0, length);
        this.sender = packet.getAddress();
    }

    public final InetAddress getSender() {
        return this.sender;
    }

    public final byte[] getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return new String(data, 0, data.length, StandardCharsets.UTF_8);
    }
}
