package de.poweruser.powerserver.games;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.QueryInfo;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public abstract class GameServerBase implements GameServerInterface {

    private int queryPort;
    private long lastHeartbeat;
    private QueryBuffer queryBuffer;
    private boolean hasAnswered;
    protected MessageData queryInfo;
    protected GameBase game;
    protected InetAddress serverAddress;

    public GameServerBase(GameBase game) {
        this.game = game;
        this.queryInfo = new MessageData();
        this.hasAnswered = false;
        this.queryBuffer = new QueryBuffer();
    }

    @Override
    public void incomingHeartbeat(InetSocketAddress serverAddress, MessageData data) {
        if(data.isHeartBeat()) {
            this.lastHeartbeat = System.currentTimeMillis();
            this.serverAddress = serverAddress.getAddress();
            this.setQueryPort(data);
        }
    }

    @Override
    public void incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data) {
        if(data.isHeartBeatBroadcast()) {
            this.lastHeartbeat = System.currentTimeMillis();
            this.serverAddress = serverAddress.getAddress();
            this.setQueryPort(data);
        }
    }

    @Override
    public void incomingQueryAnswer(InetSocketAddress serverAddress, MessageData data) {
        if(this.queryBuffer.put(data)) {
            MessageData completeQuery = this.queryBuffer.getQueryIfComplete();
            if(completeQuery != null) {
                this.processNewMessage(completeQuery);
            }
        }
    }

    @Override
    public int getQueryPort() {
        return this.queryPort;
    }

    @Override
    public boolean checkLastHeartbeat(long timeDiff) {
        return (System.currentTimeMillis() - this.lastHeartbeat) < timeDiff;
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

    @Override
    public void processNewMessage(MessageData completeQuery) {
        if(completeQuery.isQueryAnswer()) {
            this.queryInfo.update(completeQuery);
            if(!this.hasAnswered) {
                String logMessage = "New server for game " + this.game.getGameDisplayName(this) + ": " + this.serverAddress.getHostAddress();
                String gamePort = this.game.getGamePort(this);
                if(gamePort != null) {
                    logMessage += ":" + gamePort;
                }
                Logger.logStatic(logMessage);
            }
            this.hasAnswered = true;
        }
    }

    @Override
    public boolean hasAnsweredToQuery() {
        return this.hasAnswered;
    }

    public MessageData getQueryInfo() {
        return this.queryInfo;
    }

    private class QueryBuffer {
        private Map<Integer, MessageData> queries;
        private Map<Integer, QueryInfo> infos;
        private int lastId;

        public QueryBuffer() {
            this.queries = new HashMap<Integer, MessageData>();
            this.infos = new HashMap<Integer, QueryInfo>();
            this.lastId = 0;
        }

        public boolean put(MessageData query) {
            if(query.isQueryAnswer()) {
                QueryInfo info = query.getQueryInfo();
                if(info.getId() >= this.lastId) {
                    this.lastId = info.getId();
                    int part = info.getPart();
                    if(part >= 1) {
                        this.queries.put(part, query);
                        this.infos.put(part, info);
                        return true;
                    }
                }
            }
            return false;
        }

        public MessageData getQueryIfComplete() {
            boolean ok = true;
            int lastPart = -1;
            int i = 1;
            while(ok) {
                QueryInfo info = this.infos.get(i);
                if(ok = (info != null && info.getId() == this.lastId && info.getPart() == i)) {
                    if(info.isFinal()) {
                        lastPart = info.getPart();
                        break;
                    }
                }
                i++;
            }
            MessageData out = null;
            if(ok && lastPart >= 1) {
                out = this.queries.get(1);
                for(int j = 2; j <= lastPart; j++) {
                    out = out.combine(this.queries.get(j));
                }
                this.clear();
            }
            return out;
        }

        private void clear() {
            this.queries.clear();
            this.infos.clear();
        }
    }
}
