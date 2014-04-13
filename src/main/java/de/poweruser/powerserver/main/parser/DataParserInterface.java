package de.poweruser.powerserver.main.parser;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.network.UDPMessage;

public interface DataParserInterface {
    public MessageData parse(GameBase game, UDPMessage msg) throws ParserException;
}
