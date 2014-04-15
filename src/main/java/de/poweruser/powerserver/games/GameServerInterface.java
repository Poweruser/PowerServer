package de.poweruser.powerserver.games;

import java.net.InetAddress;

import de.poweruser.powerserver.main.MessageData;

public interface GameServerInterface {
    public void processNewMessage(MessageData message);

    public void incomingHeartbeat(InetAddress server, MessageData message);

    public void incomingHeartBeatBroadcast(InetAddress sender, InetAddress server, MessageData data);
}
