package de.poweruser.powerserver.games;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.QueryInfo;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public abstract class GameServerBase implements GameServerInterface {

    private int queryPort;
    private long lastHeartbeat;
    private long lastMessage;
    private long lastQueryRequest;
    private boolean realHeartbeat;
    private QueryBuffer queryBuffer;
    private boolean hasAnswered;
    protected MessageData queryInfo;
    protected GameBase game;
    protected InetSocketAddress serverAddress;

    public GameServerBase(GameBase game, InetSocketAddress server) {
        this.lastHeartbeat = -1L;
        this.lastMessage = -1L;
        this.lastQueryRequest = -1L;
        this.realHeartbeat = false;
        this.game = game;
        this.queryInfo = new MessageData();
        this.hasAnswered = false;
        this.queryBuffer = new QueryBuffer();
        this.serverAddress = server;
    }

    @Override
    public boolean incomingHeartbeat(InetSocketAddress serverAddress, MessageData data) {
        if(data.isHeartBeat() && this.serverAddress.equals(serverAddress)) {
            this.realHeartbeat = true;
            this.setQueryPort(data);
            return this.newHeartBeat();
        }
        return false;
    }

    private boolean newHeartBeat() {
        boolean firstHeartBeat = this.lastHeartbeat < 0L;
        long currentTime = System.currentTimeMillis();
        this.lastHeartbeat = currentTime;
        this.lastMessage = currentTime;
        return firstHeartBeat;
    }

    @Override
    public boolean incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data) {
        if(data.isHeartBeatBroadcast() && this.isBroadcastedServer() && this.serverAddress.equals(serverAddress)) {
            this.setQueryPort(data);
            return this.newHeartBeat();
        }
        return false;
    }

    @Override
    public void incomingQueryAnswer(InetSocketAddress serverAddress, MessageData data) {
        if(this.serverAddress.equals(serverAddress)) {
            if(this.queryBuffer.put(data)) {
                MessageData completeQuery = this.queryBuffer.getQueryIfComplete();
                if(completeQuery != null) {
                    this.processNewMessage(completeQuery);
                }
            }
        }
    }

    @Override
    public int getQueryPort() {
        return this.queryPort;
    }

    @Override
    public boolean checkLastHeartbeat(long timeDiff, TimeUnit unit) {
        return (System.currentTimeMillis() - this.lastHeartbeat) < TimeUnit.MILLISECONDS.convert(timeDiff, unit);
    }

    @Override
    public boolean checkLastMessage(long timeDiff, TimeUnit unit) {
        return (System.currentTimeMillis() - this.lastMessage) < TimeUnit.MILLISECONDS.convert(timeDiff, unit);
    }

    @Override
    public boolean checkLastQueryRequest(long timeDiff, TimeUnit unit) {
        return (System.currentTimeMillis() - this.lastQueryRequest) < TimeUnit.MILLISECONDS.convert(timeDiff, unit);
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
                String logMessage = "New server for game " + this.getDisplayName() + ": " + this.serverAddress.getAddress().getHostAddress();
                String gamePort = this.getGamePort();
                if(gamePort != null) {
                    logMessage += ":" + gamePort;
                }
                String name = this.getServerName();
                if(name != null) {
                    logMessage += " -> " + name;
                }
                Logger.logStatic(LogLevel.NORMAL, logMessage);
            }
            this.hasAnswered = true;
            this.lastMessage = System.currentTimeMillis();
        }
    }

    @Override
    public boolean hasAnsweredToQuery() {
        return this.hasAnswered;
    }

    @Override
    public void markQueryRequestAsSentWithCurrentTime() {
        this.lastQueryRequest = System.currentTimeMillis();
    }

    public MessageData getQueryInfo() {
        return this.queryInfo;
    }

    @Override
    public boolean isBroadcastedServer() {
        return !this.realHeartbeat;
    }

    public String getDisplayName() {
        return this.game.getGameDisplayName(this);
    }

    public String getGamePort() {
        return this.game.getGamePort(this);
    }

    public GameBase getGame() {
        return this.game;
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
