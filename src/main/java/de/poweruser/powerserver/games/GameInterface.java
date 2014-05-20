package de.poweruser.powerserver.games;

import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.network.UDPMessage;

public interface GameInterface {
    public String getGameName();

    public String getGameDisplayName(GameServerBase gameServer);

    public String getGamePort(GameServerBase gameServer);

    public MessageData parseMessage(UDPMessage msg) throws ParserException;

    public boolean verifyDataKeyAndValue(String key, String value);

    public DataKeysInterface getHeartBeatDataKey();

    public DataKeysInterface getHeartBeatBroadcastDataKey();

    public GameServerInterface createNewServer();
}
