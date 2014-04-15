package de.poweruser.powerserver.games;

import java.net.InetAddress;

import de.poweruser.powerserver.main.MessageData;

public abstract class GameServerBase implements GameServerInterface {

    private int queryPort;
    private long lastHeartbeat;

    @Override
    public void incomingHeartbeat(InetAddress sender, MessageData data) {

    }

    @Override
    public void incomingHeartBeatBroadcast(InetAddress sender, InetAddress server, MessageData data) {

    }
}
