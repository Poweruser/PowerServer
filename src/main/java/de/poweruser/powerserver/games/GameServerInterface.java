package de.poweruser.powerserver.games;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.poweruser.powerserver.main.MessageData;

public interface GameServerInterface {
    public void processNewMessage(MessageData message);

    public void incomingHeartbeat(InetSocketAddress server, MessageData message);

    public void incomingHeartBeatBroadcast(InetAddress sender, InetSocketAddress server, MessageData data);

    public int getQueryPort();
}
