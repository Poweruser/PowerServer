package de.poweruser.powerserver.main.parser;

import java.util.HashMap;
import java.util.Map;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
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
        HashMap<String, String> map = new HashMap<String, String>();
        int i = 0;
        for(i = 0; i < split.length - 1; i = i + 2) {
            String key = split[i];
            if(key.trim().isEmpty()) {
                i--;
                continue;
            }
            String value = split[i + 1];
            this.processPair(map, game, key, value, message);
            if(this.isKeyFinalKey(key)) {
                break;
            }
        }
        if(i == split.length - 1) {
            String key = split[i];
            if(this.isKeyFinalKey(key)) {
                this.processPair(map, game, key, "", message);
            }
        }
        return new MessageData(map);
    }

    private void processPair(Map<String, String> map, GameBase game, String key, String value, String message) throws ParserException {
        if(game == null || game.verifyDataKeyAndValue(key, value)) {
            map.put(key, value);
        } else {
            throw new ParserException("Found invalid data key \"" + key + "\" and value \"" + value + "\".", message, game);
        }
    }

    private boolean isKeyFinalKey(String key) {
        return GeneralDataKeysEnum.FINAL.toString().equalsIgnoreCase(key);
    }
}
