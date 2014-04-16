package de.poweruser.powerserver.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.PowerServer;

public class UDPSender {

    private DatagramSocket udpSocket;

    public UDPSender(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    public void broadcastHeartBeat(List<InetAddress> masterServers, DatagramPacket packet) {
        for(InetAddress ms: masterServers) {
            packet.setAddress(ms);
            packet.setPort(PowerServer.MASTERSERVER_UDP_PORT);
            try {
                this.udpSocket.send(packet);
            } catch(IOException e) {
                Logger.logStatic("Failed to send a heartbeatbroadcast to a masterserver at " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ". Content: " + new String(packet.getData()));
            }
        }
    }
}
