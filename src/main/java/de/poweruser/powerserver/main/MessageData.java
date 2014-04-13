package de.poweruser.powerserver.main;

import java.util.HashMap;

import de.poweruser.powerserver.games.DataKeysInterface;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;

public class MessageData {

    private HashMap<String, String> map;

    public MessageData(HashMap<String, String> map) {
        this.map = map;
    }

    public boolean containsKey(DataKeysInterface key) {
        return this.map.containsKey(key.getKeyString());
    }

    public String getData(DataKeysInterface key) {
        return this.map.get(key.getKeyString());
    }

    public boolean isHeartBeat() {
        return this.containsKey(GeneralDataKeysEnum.HEARTBEAT);
    }

    public boolean isHeartBeatBroadcast() {
        return this.containsKey(GeneralDataKeysEnum.HEARTBEATBROADCAST);
    }

    public GameBase getGame() {
        return GameBase.getGameForGameName(this.getData(GeneralDataKeysEnum.GAMENAME));
    }
}
