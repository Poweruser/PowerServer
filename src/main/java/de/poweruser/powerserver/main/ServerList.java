package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerInterface;

public class ServerList {

    private GameBase game;
    private Map<InetAddress, GameServerInterface> servers;

    public ServerList(GameBase game) {
        this.game = game;
        this.servers = new HashMap<InetAddress, GameServerInterface>();
    }

    public void incomingHeartBeat(InetAddress sender, MessageData data) {
        // TODO Auto-generated method stub

    }

    public void incomingHeartBeatBroadcast(InetAddress sender, MessageData data) {
        // TODO Auto-generated method stub

    }

    public void incomingQueryAnswer(InetAddress sender, MessageData data) {
        // TODO Auto-generated method stub

    }
}
