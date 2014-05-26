package de.poweruser.powerserver.games;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.network.UDPMessage;

/**
 * This interface must be implemented by all games in PowerServer. The games
 * usually do not directly implement it, but inherit it from the abstract class
 * GameBase that they extend. Most methods of this interface are already covered
 * by GameBase, but not all.
 * 
 * @author Poweruser
 * 
 */

public interface GameInterface {

    /**
     * Getter method for the game name, that is the key for the game that
     * GameSpy uses to identify it
     * 
     * @return The GameSpy key that identifies the game, as a String
     */
    public String getGameName();

    /**
     * Gets the display name of the game that is currently running on
     * gameServer. The display name may vary within a game, with different
     * versions of it for example.
     * The game that is represented by the gameServer must match the game that
     * this is method is called upon. If it does not match an
     * IllegalArgumentException is thrown
     * 
     * @param gameServer
     *            the server that the display name shall be generated for
     * @return The display name of the game that is represented by gameServer
     * @throws IllegalArgumentException
     *             When the both games, the game that getGameDisplayName is
     *             called on and the game of the gameServer, do not match
     */

    public String getGameDisplayName(GameServerBase gameServer) throws IllegalArgumentException;

    /**
     * Gets the game port that the passed game server is running on. If that
     * information was not received through a query answer yet, this method
     * returns null.
     * The game that is represented by the gameServer must match the game that
     * this is method is called upon. If it does not match an
     * IllegalArgumentException is thrown
     * 
     * @param gameServer
     *            the server of which its game port shall be returned
     * @return the game port the server is running on as a String. If none is
     *         set yet, it returns null
     * @throws IllegalArgumentException
     *             An IllegalArgumentException is thrown, when the both games,
     *             the game that getGamePort is called on and the game of the
     *             gameServer, do not match
     */

    public String getGamePort(GameServerBase gameServer) throws IllegalArgumentException;

    /**
     * Parses a UDPMessage specifically for this game. The game uses the parser
     * that is assigned to it for this task.
     * 
     * @param msg
     *            The UDPMessage to parse
     * @return The parsed data as a MessageData object
     * @throws ParserException
     *             A ParserException is thrown when an error happens during
     *             parsing or when the input is in a unexpected or corrupt
     *             format. When, or in what cases a ParserException is thrown
     *             completely depends on the parser that the game, this method
     *             is
     *             called upon, uses.
     */

    public MessageData parseMessage(UDPMessage msg) throws ParserException;

    /**
     * Verifies a key-value pair based on the available data keys for this game.
     * These include the general data keys defined in
     * de.poweruser.powerserver.games.GeneralDataKeysEnum and the ones that were
     * additionally defined for this game. This method returns only true if a
     * matching data key could be found and the value passes the test of its
     * assigned verifier. In all other cases this method returns false.
     * 
     * @param key
     *            The data key of the key-value pair to check, as a String
     * @param value
     *            The value of the key-value pair to check, as a String
     * @return true, if a matching data key was found and the value passes the
     *         test of its assigned verifier, otherwise false
     */

    public boolean verifyDataKeyAndValue(String key, String value);

    /**
     * Returns the data key of this game, that is responsible for marking a sent
     * message as a server heart-beat.
     * 
     * @return the heart-beat data key or null if the game has none
     */

    public DataKeysInterface getHeartBeatDataKey();

    /**
     * Returns the data key of this game, that is responsible for marking a sent
     * message as a server heart-beat broadcast.
     * 
     * @return the heart-beat broadcast data key or null if the game has none
     */

    public DataKeysInterface getHeartBeatBroadcastDataKey();

    public DatagramPacket createHeartbeatBroadcast(InetSocketAddress server, MessageData data);

    public GameServerInterface createNewServer(InetSocketAddress server);

    public DatagramPacket createStatusQuery(boolean queryPlayers);
}
