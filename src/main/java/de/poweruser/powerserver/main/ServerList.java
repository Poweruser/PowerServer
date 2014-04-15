package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerInterface;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.logger.Logger;

public class ServerList {

    private GameBase game;
    private Map<InetSocketAddress, GameServerInterface> servers;

    public ServerList(GameBase game) {
        this.game = game;
        this.servers = new HashMap<InetSocketAddress, GameServerInterface>();
    }

    public void incomingHeartBeat(InetSocketAddress sender, MessageData data) {
        if(sender != null) {
            GameServerInterface server = this.getOrCreateServer(sender);
            server.incomingHeartbeat(sender, data);
        }
    }

    public void incomingHeartBeatBroadcast(InetAddress sender, MessageData data) {
        if(data.containsKey(GeneralDataKeysEnum.HEARTBEATBROADCAST) && data.containsKey(GeneralDataKeysEnum.HOST)) {
            InetSocketAddress serverAddress = data.constructQuerySocketAddress(sender);
            GameServerInterface server = this.getOrCreateServer(serverAddress);
            server.incomingHeartBeatBroadcast(sender, serverAddress, data);
        } else {
            Logger.logStatic("Got a heartbeatbroadcast, that is missing the host key");
        }
    }

    public void incomingQueryAnswer(InetSocketAddress sender, MessageData data) {
        GameServerInterface server = this.getOrCreateServer(sender);
        server.processNewMessage(data);
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
}
