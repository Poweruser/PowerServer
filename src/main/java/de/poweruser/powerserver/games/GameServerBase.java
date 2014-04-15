package de.poweruser.powerserver.games;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public abstract class GameServerBase implements GameServerInterface {

    private int queryPort;
    private long lastHeartbeat;

    @Override
    public void incomingHeartbeat(InetSocketAddress sender, MessageData data) {
        if(data.isHeartBeat()) {
            this.lastHeartbeat = System.currentTimeMillis();
            this.setQueryPort(data);
        }
    }

    @Override
    public void incomingHeartBeatBroadcast(InetAddress sender, InetSocketAddress server, MessageData data) {
        if(data.isHeartBeatBroadcast()) {
            this.lastHeartbeat = System.currentTimeMillis();
            this.setQueryPort(data);
        }
    }

    @Override
    public int getQueryPort() {
        return this.queryPort;
    }

    public boolean checkLastHeartbeat(long timeDiff) {
        return (System.currentTimeMillis() - this.lastHeartbeat) > timeDiff;
    }

    private void setQueryPort(MessageData data) {
        String queryPort = null;
        if(data.isHeartBeat()) {
            queryPort = data.getData(GeneralDataKeysEnum.HEARTBEAT);
        } else if(data.isHeartBeatBroadcast()) {
            queryPort = data.getData(GeneralDataKeysEnum.HEARTBEATBROADCAST);
        }
        IntVerify verifier = new IntVerify(1024, 65535);
        if(verifier.verify(queryPort)) {
            this.queryPort = verifier.getVerifiedValue();
        }
    }
}
