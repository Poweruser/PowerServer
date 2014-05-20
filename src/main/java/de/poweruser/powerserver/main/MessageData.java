package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import de.poweruser.powerserver.games.DataKeysInterface;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.main.parser.dataverification.IPAddressVerify;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;
import de.poweruser.powerserver.main.parser.dataverification.QueryIdFormatVerify;

public class MessageData implements CombineableInterface<MessageData> {

    private HashMap<String, String> map;

    public MessageData() {
        this.map = new HashMap<String, String>();
    }

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

    public boolean hasStateChanged() {
        GeneralDataKeysEnum key = GeneralDataKeysEnum.STATECHANGED;
        if(this.containsKey(key)) {
            String data = this.getData(key);
            IntVerify verifier = new IntVerify(1, 1);
            return verifier.verify(data);
        }
        return false;
    }

    public boolean isQueryAnswer() {
        return this.containsKey(GeneralDataKeysEnum.QUERYID);
    }

    public InetSocketAddress constructQuerySocketAddress(InetSocketAddress sender) {
        InetAddress server = null;
        int queryPort = 0;
        IntVerify intVerifier = new IntVerify(1024, 65535);
        if(this.isHeartBeat()) {
            server = sender.getAddress();
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
        } else if(this.isQueryAnswer()) { return sender; }
        if(server != null && queryPort != 0) { return new InetSocketAddress(server, queryPort); }
        return null;
    }

    public GameBase getGame() {
        return GameBase.getGameForGameName(this.getData(GeneralDataKeysEnum.GAMENAME));
    }

    @Override
    public MessageData combine(MessageData combineable) {
        MessageData combination = new MessageData();
        combination.map.putAll(combineable.map);
        combination.map.putAll(this.map);
        return combination;
    }

    @Override
    public void update(MessageData combineable) {
        this.map.putAll(combineable.map);
    }

    public QueryInfo getQueryInfo() {
        if(this.isQueryAnswer()) {
            QueryIdFormatVerify verifier = new QueryIdFormatVerify();
            QueryInfo info = null;
            if(verifier.verify(this.getData(GeneralDataKeysEnum.QUERYID))) {
                info = verifier.getVerifiedQueryInfo();
                if(this.containsKey(GeneralDataKeysEnum.FINAL)) {
                    info.setFinal();
                }
                return info;
            }
        }
        return null;
    }
}
