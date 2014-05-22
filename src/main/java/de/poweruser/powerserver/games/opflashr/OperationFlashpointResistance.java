package de.poweruser.powerserver.games.opflashr;

import de.poweruser.powerserver.games.DataKeysInterface;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerBase;
import de.poweruser.powerserver.games.GameServerInterface;
import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.parser.DataParserInterface;

public class OperationFlashpointResistance extends GameBase {

    private String displayName = "Operation Flashpoint:Resistance";

    public OperationFlashpointResistance(String gamename, String gamespyKey, DataParserInterface parser) {
        super(gamename, gamespyKey, parser, DataKeyEnum.values());
        this.adjustPlayerInfoDataKeys();
    }

    private void adjustPlayerInfoDataKeys() {
        DataKeyEnum player = DataKeyEnum.PLAYER_;
        DataKeyEnum score = DataKeyEnum.SCORE_;
        DataKeyEnum deaths = DataKeyEnum.DEATHS_;
        DataKeyEnum team = DataKeyEnum.TEAM_;
        this.keyMap.remove(player.getKeyString());
        this.keyMap.remove(score.getKeyString());
        this.keyMap.remove(deaths.getKeyString());
        this.keyMap.remove(team.getKeyString());
        for(int i = 0; i <= 30; i++) {
            String index = String.valueOf(i);
            this.keyMap.put(player.getKeyString() + index, player);
            this.keyMap.put(score.getKeyString() + index, score);
            this.keyMap.put(deaths.getKeyString() + index, deaths);
            this.keyMap.put(team.getKeyString() + index, team);
        }
    }

    @Override
    public DataKeysInterface getHeartBeatDataKey() {
        return DataKeyEnum.HEARTBEAT;
    }

    @Override
    public DataKeysInterface getHeartBeatBroadcastDataKey() {
        return DataKeyEnum.HEARTBEATBROADCAST;
    }

    @Override
    public GameServerInterface createNewServer() {
        return new OFPServer(this);
    }

    @Override
    public String getGameDisplayName(GameServerBase gameServer) {
        MessageData data = gameServer.getQueryInfo();
        String out = this.displayName;
        String version = null;
        if(data.containsKey(DataKeyEnum.GAMEVERSION)) {
            version = data.getData(DataKeyEnum.GAMEVERSION);
        } else if(data.containsKey(DataKeyEnum.ACTUALVERSION)) {
            version = data.getData(DataKeyEnum.ACTUALVERSION);
        }
        if(version != null) {
            if(version.equals("1.99")) {
                out = "Arma:ColdWarAssault";
            }
            out += " (version " + version + ")";
        }
        return out;
    }

    @Override
    public String getGamePort(GameServerBase gameServer) {
        MessageData data = gameServer.getQueryInfo();
        String out = null;
        if(data.containsKey(DataKeyEnum.HOSTPORT)) {
            out = data.getData(DataKeyEnum.HOSTPORT);
        }
        return out;
    }
}
