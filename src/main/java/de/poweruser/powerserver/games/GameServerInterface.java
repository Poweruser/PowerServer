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
     * longer ago, than the specified time duration.
     * 
     * @param timeDiff
     *            The time duration to check
     * @param unit
     *            The time unit of the provided time duration
     * @return true, if the last heart-beat was received within the specified
     *         duration, or if no heart-beat has been received yet
     *         false, if a heart-beat has been received already and it was
     *         received longer ago than the specified duration
     */

    public boolean checkLastHeartbeat(long timeDiff, TimeUnit unit);

    /**
     * Checks whether the last query reply of this server has been received not
     * longer ago, than the specified time duration
     * 
     * @param timeDiff
     *            The time duration to check
     * @param unit
     *            The time unit of the provided time duration
     * @return true, if the last query reply was received within the specified
     *         duration, or if no query reply has been received yet
     *         false, if a query reply has been received already and it was
     *         received longer ago than the specified duration
     */

    public boolean checkLastQueryReply(long timeDiff, TimeUnit unit);

    /**
     * Checks whether the last query request of this server has been sent not
     * longer ago, than the specified time duration
     * 
     * @param timeDiff
     *            The time duration to check
     * @param unit
     *            The time unit of the provided time duration
     * @return true, if the last query request was sent within the specified
     *         duration, or if no query request has been sent yet
     *         false, if a query request has been sent yet and it was sent
     *         longer ago than the specified duration
     */

    public boolean checkLastQueryRequest(long timeDiff, TimeUnit unit);

    /**
     * Checks if this server has answered to a query request yet
     * 
     * @return true, if a query answer has been received from this server yet,
     *         otherwise false
     */
    public boolean hasAnsweredToQuery();

    /**
     * Checks if another master server is sending heart-beat broadcasts
     * for this game server, or if the master server is receiving the real
     * heart-beats from this game server.
     * 
     * @return true, if another master server is sending heart-beat broadcasts
     *         for this game server
     *         false, if this master server is receiving the real heart-beats of
     *         the game server
     */

    public boolean isBroadcastedServer();

    /**
     * Gets the name of the game server, if no name of the game server has been
     * received yet, it returns null
     * 
     * @return The server name if one has been received yet.
     *         null, if none has been received yet
     */

    public String getServerName();

    public void queryWasSent();
}
