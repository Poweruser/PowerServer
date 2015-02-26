package de.poweruser.powerserver.games;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.ServerList;
import de.poweruser.powerserver.main.parser.DataParserInterface;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.network.UDPMessage;
import de.poweruser.powerserver.settings.Settings;

public abstract class GameBase implements GameInterface {

    private static Map<String, GameBase> gameNameMap = new HashMap<String, GameBase>();
    protected Map<String, DataKeysInterface> keyMap;
    protected String gamename;
    protected String gamespyKey;
    protected DataParserInterface parser;
    private ServerList serverList;
    private Settings settings;

    protected GameBase(String gamename, String gamespyKey, DataParserInterface parser, DataKeysInterface[] dataKeys) {
        this.gamename = gamename;
        if(!gameNameMap.containsKey(this.getGameName())) {
            gameNameMap.put(this.getGameName(), this);
        }
        this.gamespyKey = gamespyKey;
        this.parser = parser;
        this.keyMap = new HashMap<String, DataKeysInterface>();
        for(GeneralDataKeysEnum datakey: GeneralDataKeysEnum.values()) {
            this.keyMap.put(datakey.getKeyString(), datakey);
        }
        for(DataKeysInterface d: dataKeys) {
            this.keyMap.put(d.getKeyString(), d);
        }
        this.serverList = new ServerList(this);
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Settings getSettings() {
        return this.settings;
    }

    @Override
    public String getGameName() {
        return this.gamename;
    }

    public String getGamespyKey() {
        return this.gamespyKey;
    }

    public static GameBase getGameForGameName(String gamename) {
        if(gamename != null) { return gameNameMap.get(gamename); }
        return null;
    }

    @Override
    public MessageData parseMessage(UDPMessage msg) throws ParserException {
        return this.parser.parse(this, msg);
    }

    @Override
    public boolean verifyDataKeyAndValue(String key, String value) {
        if(this.keyMap.containsKey(key)) {
            DataKeysInterface dataKey = this.keyMap.get(key);
            return dataKey.verifyData(value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.gamename.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && o instanceof GameBase && ((GameBase) o).getGameName().equalsIgnoreCase(this.gamename));
    }

    public List<InetSocketAddress> getActiveServers() {
        return this.serverList.getActiveServers(this.settings);
    }

    public ServerList getServerList() {
        return this.serverList;
    }
}
