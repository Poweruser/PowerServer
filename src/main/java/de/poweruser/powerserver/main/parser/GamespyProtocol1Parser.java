package de.poweruser.powerserver.main.parser;

import java.util.HashMap;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.network.UDPMessage;

public class GamespyProtocol1Parser implements DataParserInterface {

    @Override
    public MessageData parse(GameBase game, UDPMessage msg) throws ParserException {
        return this.parse(game, msg.toString());
    }

    @Override
    public MessageData parse(GameBase game, String message) throws ParserException {
        int start = message.indexOf("\\");
        if(start < 0) { throw new ParserException("The message does not contain a backslash \"\\\". Is it a gamespy protocol version 2 message?", message, game); }
        message = message.substring(start + 1);
        String[] split = message.split("\\\\");
        if((split.length % 2) != 0) { throw new ParserException("The number of elements in a gamespy protocol version 1 message must be even.", message, game); }
        HashMap<String, String> map = new HashMap<String, String>();
        for(int i = 0; i < split.length - 1; i = i + 2) {
            String key = split[i];
            String value = split[i + 1];
            if(game == null || game.verifyDataKeyAndValue(key, value)) {
                map.put(key, value);
            } else {
                throw new ParserException("Found invalid data key \"" + key + "\" and value \"" + value + "\".", message, game);
            }
        }
        return new MessageData(map);
    }
}
