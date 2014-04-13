package de.poweruser.powerserver.games.opflashr;

import de.poweruser.powerserver.games.DataKeysInterface;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;
import de.poweruser.powerserver.main.parser.dataverification.QueryIdFormatVerify;
import de.poweruser.powerserver.main.parser.dataverification.StringVerify;
import de.poweruser.powerserver.main.parser.dataverification.VerificationInterface;

public enum DataKeyEnum implements DataKeysInterface {
    ACTUALVERSION("actver", new IntVerify(100, Integer.MAX_VALUE)),
    EQUALMODREQUIRED("equalModReq", new IntVerify(0, 1)),
    FINAL("final", new StringVerify(new String[] { "" }, false)),
    GAMEMODE("gamemode"),
    GAMENAME("gamename"),
    GAMESTATE("gstate", new IntVerify(0, 14)),
    GROUPID("groupid", new IntVerify(261, 261)),
    HEARTBEAT("heartbeat", new IntVerify(1024, 65535)),
    HEARTBEATBROADCAST("heartbeatbroadcast", new IntVerify(1024, 65535)),
    HOSTNAME("hostname"),
    HOSTPORT("hostport", new IntVerify(1024, 65535)),
    IMPLENTATION("impl", new StringVerify(new String[] { "sockets", "dplay" }, false)),
    ISLANDNAME("mapname"),
    LOADEDMODS("mod"),
    MAXPLAYERS("maxplayers", new IntVerify(0, 128)),
    MISSIONNAME("gametype"),
    NUMBEROFPLAYERS("numplayers", new IntVerify(0, 128)),
    PARAMETER1("param1", new IntVerify(Integer.MIN_VALUE, Integer.MAX_VALUE)),
    PARAMETER2("param2", new IntVerify(Integer.MIN_VALUE, Integer.MAX_VALUE)),
    PASSWORD("password", new IntVerify(0, 1)),
    PLATFORM("platform", new StringVerify(new String[] { "win", "linux" }, false)),
    QUERYID("queryid", new QueryIdFormatVerify()),
    REQUIREDVERSION("reqver", new IntVerify(100, Integer.MAX_VALUE)),
    TIMELEFT("timeleft", new IntVerify(0, 1440)),

    // Special cases
    PLAYER_("player_"),
    SCORE_("score_", new IntVerify(Integer.MIN_VALUE, Integer.MAX_VALUE)),
    DEATHS_("deaths_", new IntVerify(0, Integer.MAX_VALUE)),
    TEAM_("team_");

    private String key;
    private VerificationInterface verifier;

    private DataKeyEnum(String key) {
        this.key = key;
        this.verifier = null;
    }

    private DataKeyEnum(String key, VerificationInterface verifier) {
        this.key = key;
        this.verifier = verifier;
    }

    @Override
    public String getKeyString() {
        return this.key;
    }

    @Override
    public boolean verifyData(String data) {
        if(this.verifier == null || this.verifier.verify(data)) {
            return true;
        } else {
            return false;
        }
    }
}
