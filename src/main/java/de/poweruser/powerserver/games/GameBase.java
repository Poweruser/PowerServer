package de.poweruser.powerserver.games;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.ServerList;
import de.poweruser.powerserver.main.parser.DataParserInterface;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.network.UDPMessage;

public abstract class GameBase implements GameInterface {

    private static Map<String, GameBase> gameNameMap = new HashMap<String, GameBase>();
    protected Map<String, DataKeysInterface> keyMap;
    protected String gamename;
    protected String gamespyKey;
    protected DataParserInterface parser;
    private ServerList serverList;

    protected GameBase(String gamename, String gamespyKey, DataParserInterface parser, DataKeysInterface[] dataKeys) {
        if(gameNameMap.containsKey(this.getGameName())) {
            gameNameMap.put(this.getGameName(), this);
        }
        this.gamespyKey = gamespyKey;
        this.parser = parser;
        this.keyMap = new HashMap<String, DataKeysInterface>();
        for(DataKeysInterface d: dataKeys) {
            this.keyMap.put(d.getKeyString(), d);
        }
        this.gamename = gamename;
        this.serverList = new ServerList(this);
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

    public DatagramPacket createHeartbeatBroadcast(InetSocketAddress server, MessageData data) {
        if(data.containsKey(GeneralDataKeysEnum.HEARTBEAT)) {
            StringBuilder builder = new StringBuilder();
            builder.append("\\");
            builder.append(GeneralDataKeysEnum.HEARTBEATBROADCAST.getKeyString());
            builder.append("\\");
            builder.append(data.getData(GeneralDataKeysEnum.HEARTBEAT));
            builder.append("\\");
            builder.append(GeneralDataKeysEnum.HOST.getKeyString());
            builder.append("\\");
            builder.append(server.getAddress().getHostAddress());
            builder.append("\\");
            builder.append(this.gamename);
            if(data.containsKey(GeneralDataKeysEnum.STATECHANGED)) {
                builder.append("\\");
                builder.append(GeneralDataKeysEnum.STATECHANGED.getKeyString());
                builder.append("\\");
                builder.append(data.getData(GeneralDataKeysEnum.STATECHANGED));
            }
            byte[] message = builder.toString().getBytes();
            return new DatagramPacket(message, message.length);
        }
        return null;
    }

    public DatagramPacket createStatusQuery(boolean queryPlayers) {
        StringBuilder builder = new StringBuilder();
        builder.append("\\");
        if(queryPlayers) {
            builder.append("info\\rules\\players");
        } else {
            builder.append("status");
        }
        builder.append("\\");
        byte[] message = builder.toString().getBytes();
        return new DatagramPacket(message, message.length);
    }

    public List<InetSocketAddress> getActiveServers() {
        return this.serverList.getActiveServers();
    }

    public ServerList getServerList() {
        return this.serverList;
    }
}
