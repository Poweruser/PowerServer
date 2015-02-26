package de.poweruser.powerserver.games;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.main.MessageData;

public interface GameServerInterface {
    public boolean processNewMessage(MessageData completeQuery);

    public boolean incomingHeartbeat(InetSocketAddress serverAddress, MessageData message, boolean manuallyAdded);

    public boolean incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data);

    public boolean incomingQueryAnswer(InetSocketAddress serverAddress, MessageData data);

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
     *         duration
     *         false, if a heart-beat has been received already and it was
     *         received longer ago than the specified duration, or if no
     *         heart-beat has been received yet
     */

    public boolean checkLastHeartbeat(long timeDiff, TimeUnit unit);

    /**
     * Checks whether the last message of this server has been received not
     * longer ago, than the specified time duration
     * 
     * @param timeDiff
     *            The time duration to check
     * @param unit
     *            The time unit of the provided time duration
     * @return true, if the last message was received within the specified
     *         duration
     *         false, if a message has been received already and it was
     *         received longer ago than the specified duration, or if no message
     *         has been received yet
     */

    public boolean checkLastMessage(long timeDiff, TimeUnit unit);

    /**
     * Checks whether the last query request of this server has been sent not
     * longer ago, than the specified time duration
     * 
     * @param timeDiff
     *            The time duration to check
     * @param unit
     *            The time unit of the provided time duration
     * @return true, if the last query request was sent within the specified
     *         duration
     *         false, if a query request has been sent yet and it was sent
     *         longer ago than the specified duration, or if no query request
     *         has been sent yet
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

    public InetSocketAddress getSocketAddress();

    /**
     * Sets a flag with the current time for the last sent query request. This
     * flag will be used when testing for the last send time of a query request
     * with the method checkLastQueryRequest(long, TimeUnit)
     */

    public void markQueryRequestAsSentWithCurrentTime();
}
