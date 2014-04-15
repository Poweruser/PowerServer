package de.poweruser.powerserver.games;

import java.net.InetAddress;

import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public abstract class GameServerBase implements GameServerInterface {

    private int queryPort;
    private long lastHeartbeat;

    @Override
    public void incomingHeartbeat(InetAddress sender, MessageData data) {
        if(data.containsKey(GeneralDataKeysEnum.HEARTBEAT)) {
            this.lastHeartbeat = System.currentTimeMillis();
            String queryPort = data.getData(GeneralDataKeysEnum.HEARTBEAT);
            IntVerify verifier = new IntVerify(1024, 65535);
            if(verifier.verify(queryPort)) {
                this.queryPort = verifier.getVerifiedValue();
            }
        }
    }

    @Override
    public int getQueryPort() {
        return this.queryPort;
    }

    public boolean checkLastHeartbeat(long timeDiff) {
        return (System.currentTimeMillis() - this.lastHeartbeat) > timeDiff;
    }
}
