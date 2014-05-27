package de.poweruser.powerserver.main.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.network.UDPMessage;

public class GamespyProtocol1Parser implements DataParserInterface {

    private GameBase parsedGame;

    @Override
    public MessageData parse(GameBase game, UDPMessage msg) throws ParserException {
        return this.parse(game, msg.toString());
    }

    @Override
    public MessageData parse(GameBase game, String message) throws ParserException {
        if(game != null) {
            this.parsedGame = game;
        }
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
            this.processPair(map, key, value, message);
            if(this.isKeyFinalKey(key)) {
                if(i + 3 < split.length) {
                    key = split[i + 2];
                    if(this.isKeyQueryKey(key)) {
                        value = split[i + 3];
                        this.processPair(map, key, value, message);
                    }
                }
                break;
            }
        }
        if(i == split.length - 1) {
            String key = split[i];
            if(this.isKeyFinalKey(key)) {
                this.processPair(map, key, "", message);
            }
        }
        return new MessageData(map);
    }

    private void processPair(Map<String, String> map, String key, String value, String message) throws ParserException {
        if(this.parsedGame == null && this.isGameNameKey(key)) {
            GameBase foundGame = GameBase.getGameForGameName(value);
            if(foundGame != null) {
                this.parsedGame = foundGame;
                for(Entry<String, String> entry: map.entrySet()) {
                    if(!this.parsedGame.verifyDataKeyAndValue(entry.getKey(), entry.getValue())) { throw new ParserException("Found invalid data key \"" + entry.getKey() + "\" and value \"" + entry.getValue() + "\".", message, this.parsedGame); }
                }
            } else {
                throw new ParserException("The gamename \"" + value + "\" in this message was not recognised.", message, this.parsedGame);
            }
        }
        if(this.parsedGame == null || this.parsedGame.verifyDataKeyAndValue(key, value)) {
            map.put(key, value);
        } else {
            throw new ParserException("Found invalid data key \"" + key + "\" and value \"" + value + "\".", message, this.parsedGame);
        }
    }

    private boolean isKeyFinalKey(String key) {
        return GeneralDataKeysEnum.FINAL.getKeyString().equalsIgnoreCase(key);
    }

    private boolean isKeyQueryKey(String key) {
        return GeneralDataKeysEnum.QUERYID.getKeyString().equalsIgnoreCase(key);
    }

    private boolean isGameNameKey(String key) {
        return GeneralDataKeysEnum.GAMENAME.getKeyString().equalsIgnoreCase(key);
    }

    public void reset() {
        this.parsedGame = null;
    }
}
