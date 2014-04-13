package de.poweruser.powerserver.network;

import java.net.DatagramSocket;
import java.util.List;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.main.MessageData;

public class UDPSender {

    private DatagramSocket udpSocket;

    public UDPSender(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    public void broadcastHeartBeat(List<String> masterServers, UDPMessage message, MessageData data, GameBase game) {

    }

}
