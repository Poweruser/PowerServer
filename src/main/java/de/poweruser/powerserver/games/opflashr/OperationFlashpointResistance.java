package de.poweruser.powerserver.games.opflashr;

import de.poweruser.powerserver.games.DataKeysInterface;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerInterface;
import de.poweruser.powerserver.main.parser.DataParserInterface;

public class OperationFlashpointResistance extends GameBase {

    public OperationFlashpointResistance(String gamename, DataParserInterface parser) {
        super(gamename, parser, DataKeyEnum.values());
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
        return new OFPServer();
    }
}
