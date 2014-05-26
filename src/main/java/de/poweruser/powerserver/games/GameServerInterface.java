package de.poweruser.powerserver.games;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.main.MessageData;

public interface GameServerInterface {
    public void processNewMessage(MessageData completeQuery);

    public boolean incomingHeartbeat(InetSocketAddress serverAddress, MessageData message);

    public boolean incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data);

    public void incomingQueryAnswer(InetSocketAddress serverAddress, MessageData data);

    public int getQueryPort();

    /**
     * Checks whether the last heart-beat for this server has been received not
     * longer ago, then the specified time duration.
     * 
     * @param timeDiff
     *            The time duration to check
     * @param unit
     * @return true, if the last heart-beat was received within the specified
     *         duration, or if no heart-beat has been received yet
     *         false, if a heart-beat has been received already and it was
     *         received longer ago than the specified duration
     */

    public boolean checkLastHeartbeat(long timeDiff, TimeUnit unit);

    public boolean checkLastQueryReply(long timeDiff, TimeUnit unit);

    public boolean checkLastQueryRequest(long timeDiff, TimeUnit unit);

    public boolean hasAnsweredToQuery();

    public boolean isBroadcastedServer();

    public String getServerName();

    public void queryWasSent();
}
