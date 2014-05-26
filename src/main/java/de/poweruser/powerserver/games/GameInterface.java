package de.poweruser.powerserver.games;

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
     * this is method is called upon
     * 
     * @param gameServer
     *            the server that the display name shall be generated for
     * @return The display name of the game that is represented by gameServer
     */

    public String getGameDisplayName(GameServerBase gameServer);

    public String getGamePort(GameServerBase gameServer);

    public MessageData parseMessage(UDPMessage msg) throws ParserException;

    public boolean verifyDataKeyAndValue(String key, String value);

    public DataKeysInterface getHeartBeatDataKey();

    public DataKeysInterface getHeartBeatBroadcastDataKey();

    public GameServerInterface createNewServer(InetSocketAddress server);
}
