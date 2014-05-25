package de.poweruser.powerserver.games.opflashr;

import java.net.InetSocketAddress;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerBase;
import de.poweruser.powerserver.main.MessageData;

public class OFPServer extends GameServerBase {

    public OFPServer(GameBase game, InetSocketAddress server) {
        super(game, server);
    }

    @Override
    public void processNewMessage(MessageData message) {
        super.processNewMessage(message);
    }

    @Override
    public String getServerName() {
        if(this.queryInfo.containsKey(DataKeyEnum.HOSTNAME)) { return this.queryInfo.getData(DataKeyEnum.HOSTNAME); }
        return null;
    }
}
