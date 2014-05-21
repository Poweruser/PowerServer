package de.poweruser.powerserver.main;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerInterface;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.logger.Logger;

public class ServerList {

    private GameBase game;
    private Map<InetSocketAddress, GameServerInterface> servers;

    private static final long allowedServerTimeout = 3600L * 1000L; // one hour

    public ServerList(GameBase game) {
        this.game = game;
        this.servers = new HashMap<InetSocketAddress, GameServerInterface>();
    }

    public void incomingHeartBeat(InetSocketAddress serverAddress, MessageData data) {
        if(serverAddress != null) {
            GameServerInterface server = this.getOrCreateServer(serverAddress);
            server.incomingHeartbeat(serverAddress, data);
        }
    }

    public void incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data) {
        if(data.containsKey(GeneralDataKeysEnum.HEARTBEATBROADCAST) && data.containsKey(GeneralDataKeysEnum.HOST)) {
            GameServerInterface server = this.getOrCreateServer(serverAddress);
            server.incomingHeartBeatBroadcast(serverAddress, data);
        } else {
            Logger.logStatic("Got a heartbeatbroadcast, that is missing the host key");
        }
    }

    public void incomingQueryAnswer(InetSocketAddress sender, MessageData data) {
        GameServerInterface server = this.getOrCreateServer(sender);
        server.incomingQueryAnswer(sender, data);
    }

    private GameServerInterface getOrCreateServer(InetSocketAddress server) {
        GameServerInterface gameServer;
        if(!this.servers.containsKey(server)) {
            gameServer = this.game.createNewServer();
            this.servers.put(server, gameServer);
        } else {
            gameServer = this.servers.get(server);
        }
        return gameServer;
    }

    public List<InetSocketAddress> getActiveServers() {
        List<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
        Iterator<Entry<InetSocketAddress, GameServerInterface>> iter = this.servers.entrySet().iterator();
        while(iter.hasNext()) {
            Entry<InetSocketAddress, GameServerInterface> entry = iter.next();
            GameServerInterface gsi = entry.getValue();
            if(gsi.checkLastHeartbeat(allowedServerTimeout)) {
                if(gsi.hasAnsweredToQuery()) {
                    list.add(entry.getKey());
                }
            } else {
                iter.remove();
            }
        }
        return list;
    }
}
