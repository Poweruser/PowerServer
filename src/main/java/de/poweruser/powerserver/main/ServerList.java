package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerInterface;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.logger.Logger;

public class ServerList {

    private GameBase game;
    private Map<InetAddress, GameServerInterface> servers;

    public ServerList(GameBase game) {
        this.game = game;
        this.servers = new HashMap<InetAddress, GameServerInterface>();
    }

    public void incomingHeartBeat(InetAddress sender, MessageData data) {
        GameServerInterface server = this.getOrCreateServer(sender);
        server.incomingHeartbeat(sender, data);
    }

    public void incomingHeartBeatBroadcast(InetAddress sender, MessageData data) {
        if(data.containsKey(GeneralDataKeysEnum.HOST)) {
            String host = data.getData(GeneralDataKeysEnum.HOST);
            InetAddress serverAddress = null;
            try {
                serverAddress = InetAddress.getByName(host);
            } catch(UnknownHostException e) {}
            if(serverAddress != null) {
                this.incomingHeartBeat(serverAddress, data);
            }
        } else {
            Logger.logStatic("Got a heartbeatbroadcast, that is missing the host key");
        }
    }

    public void incomingQueryAnswer(InetAddress sender, MessageData data) {
        GameServerInterface server = this.getOrCreateServer(sender);
        server.processNewMessage(data);
    }

    private GameServerInterface getOrCreateServer(InetAddress server) {
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
