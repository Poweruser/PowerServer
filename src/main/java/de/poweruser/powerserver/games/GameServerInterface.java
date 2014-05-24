package de.poweruser.powerserver.games;

import java.net.InetSocketAddress;

import de.poweruser.powerserver.main.MessageData;

public interface GameServerInterface {
    public void processNewMessage(MessageData completeQuery);

    public boolean incomingHeartbeat(InetSocketAddress serverAddress, MessageData message);

    public boolean incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data);

    public void incomingQueryAnswer(InetSocketAddress serverAddress, MessageData data);

    public int getQueryPort();

    public boolean checkLastHeartbeat(long timeDiff);

    public boolean checkLastQueryReply(long timeDiff);

    public boolean checkLastQueryRequest(long timeDiff);

    public boolean hasAnsweredToQuery();

    public boolean isBroadcastedServer();

    public String getServerName();

    public void queryWasSent();
}
