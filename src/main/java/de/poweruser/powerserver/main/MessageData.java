package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import de.poweruser.powerserver.games.DataKeysInterface;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.main.parser.dataverification.IPAddressVerify;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

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
        return this.containsKey(GeneralDataKeysEnum.HEARTBEATBROADCAST) && this.containsKey(GeneralDataKeysEnum.HOST);
    }

    public InetSocketAddress constructQuerySocketAddress(InetAddress sender) {
        InetAddress server = null;
        int queryPort = 0;
        IntVerify intVerifier = new IntVerify(1024, 65535);
        if(this.isHeartBeat()) {
            server = sender;
            if(intVerifier.verify(this.getData(GeneralDataKeysEnum.HEARTBEAT))) {
                queryPort = intVerifier.getVerifiedValue();
            }
        } else if(this.isHeartBeatBroadcast()) {
            IPAddressVerify verifier = new IPAddressVerify();
            if(verifier.verify(this.getData(GeneralDataKeysEnum.HOST))) {
                server = verifier.getVerifiedAddress();
            }
            if(intVerifier.verify(this.getData(GeneralDataKeysEnum.HEARTBEATBROADCAST))) {
                queryPort = intVerifier.getVerifiedValue();
            }
        }
        if(server != null && queryPort != 0) { return new InetSocketAddress(server, queryPort); }
        return null;
    }

    public GameBase getGame() {
        return GameBase.getGameForGameName(this.getData(GeneralDataKeysEnum.GAMENAME));
    }
}
