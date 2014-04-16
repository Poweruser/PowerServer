package de.poweruser.powerserver.games;

import java.net.InetSocketAddress;

import de.poweruser.powerserver.main.MessageData;

public interface GameServerInterface {
    public void processNewMessage(MessageData message);

    public void incomingHeartbeat(InetSocketAddress serverAddress, MessageData message);

    public void incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data);

    public int getQueryPort();
}
