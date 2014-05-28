package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import de.poweruser.powerserver.games.DataKeysInterface;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.main.parser.dataverification.IPAddressVerify;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;
import de.poweruser.powerserver.main.parser.dataverification.QueryIdFormatVerify;

public class MessageData implements CombineableInterface<MessageData> {

    /**
     * The key-value mapping of the stored data. The keys of this map are the
     * data keys in their String representation that
     * DataKeysInterface.getKeyString() returns. The values of this map are the
     * corresponding data values as String
     */
    private HashMap<String, String> map;

    /**
     * A constructor which initializes a empty mapping
     */

    public MessageData() {
        this.map = new HashMap<String, String>();
    }

    /**
     * A constructor which initializes the key-value mapping of this MessageData
     * with an passed mapping
     * 
     * @param map
     *            The mapping that shall be assigned to this MessageData
     */

    public MessageData(HashMap<String, String> map) {
        this.map = map;
    }

    /**
     * Checks if the mapping of this MessageData contains the passed key
     * 
     * @param key
     *            A data key that implements the DataKeysInterface
     * @return true if the mapping contains the key, otherwise false
     */

    public boolean containsKey(DataKeysInterface key) {
        return this.map.containsKey(key.getKeyString());
    }

    /**
     * Returns the data that is assigned to the passed data key within the
     * mapping of MessageData. If the mapping does not contain this key, the
     * returned value is the one that the mapping returns in this case, which
     * should be null.
     * 
     * @param key
     *            A data key that implements the DataKeysInterface
     * @return the data as String that is assigned to the passed data key. If
     *         the mapping does not contain the key, null
     */

    public String getData(DataKeysInterface key) {
        return this.map.get(key.getKeyString());
    }

    /**
     * Checks if this MessageData represents the data of a received heart-beat.
     * More specifically it checks if the mapping of this MessageData contains
     * the data key that represents the key for a heart-beat.
     * 
     * The assigned value to the heart-beat data key is verified as well. If
     * it does not pass the checks, this method returns false
     * 
     * @return true, if mapping contains a valid heart-beat data key and value.
     *         Otherwise false
     */

    public boolean isHeartBeat() {
        return this.checkKeyAndValue(GeneralDataKeysEnum.HEARTBEAT);
    }

    /**
     * Checks if this MessageData represents the data of a received heart-beat
     * broadcast. More specifically it checks if the mapping of this MessageData
     * contains a valid data key that represents the key for a heart-beat
     * broadcast and the second required key in a heart-beat broadcast, a valid
     * host address key.
     * The assigned values to the data keys are verified as well. If they do not
     * pass the checks, this method returns false
     * 
     * @return true, if the mapping contains the valid heart-beat broadcast and
     *         valid host data key and value.
     *         Otherwise false
     */

    public boolean isHeartBeatBroadcast() {
        return this.checkKeyAndValue(GeneralDataKeysEnum.HEARTBEATBROADCAST) && this.checkKeyAndValue(GeneralDataKeysEnum.HOST);
    }

    /**
     * Checks if this MessageData represents the data contains a valid
     * statechanged data key flag. This flag usually is only part of heart-beats
     * and heart-beat broadcasts, but it is not guaranteed. So check if this
     * MessageData represents a heart-beat, a heart-beat broadcast or something
     * else first.
     * The assigned value to the statechanged data key is verified as well. If
     * it does not pass the checks, this method returns false
     * 
     * @return true, if the mapping contains a valid statechanged data key and
     *         value. Otherwise false
     */

    public boolean hasStateChanged() {
        return this.checkKeyAndValue(GeneralDataKeysEnum.STATECHANGED);
    }

    /**
     * Checks if this MessageData represents a query answer. More specifically
     * this method checks if the mapping of this MessageData contains a valid
     * queryid data key.
     * The assigned value to the statechanged data key is verified as well. If
     * it does not pass the checks, this method returns false
     * 
     * @return true, if the mapping contains a valid queryid data key and value.
     *         Otherwise false
     */

    public boolean isQueryAnswer() {
        return this.checkKeyAndValue(GeneralDataKeysEnum.QUERYID);
    }

    private boolean checkKeyAndValue(GeneralDataKeysEnum key) {
        if(this.containsKey(key)) { return key.getVerifierCopy().verify(this.getData(key)); }
        return false;
    }

    /**
     * This method constructs the game server's query address from this
     * MessageData, that is used for sending queries to and receiving query
     * answers from. Depending on the type of data that is represented by this
     * MessageData (heart-beat / heart-beat broadcast / query answer) the query
     * port is stored in different locations.
     * 
     * For a heart-beat:
     * game server address: the address the message was sent from
     * game server query port: the value of the data key HEARTBEAT
     * 
     * For a heart-beat broadcast:
     * game server address: the value of the data key HOST
     * game server query port: the value of the data key HEARTBEATBROADCAST
     * 
     * For a query answer:
     * The game servers address and query port is the senders socket address
     * that the message was received from
     * 
     * @param sender
     *            The sender's InetSocketAddress that has sent this message data
     * @return The InetSocketAddress that targets the game server's query port
     */

    public InetSocketAddress constructQuerySocketAddress(InetSocketAddress sender) {
        InetAddress server = null;
        int queryPort = 0;
        IntVerify intVerifier = new IntVerify(1024, 65535);
        if(this.isHeartBeat()) {
            server = sender.getAddress();
            if(intVerifier.verify(this.getData(GeneralDataKeysEnum.HEARTBEAT))) {
                queryPort = intVerifier.getVerifiedValue();
            }
        } else if(this.isHeartBeatBroadcast()) {
            IPAddressVerify verifier = new IPAddressVerify();
            if(verifier.verify(this.getData(GeneralDataKeysEnum.HOST))) {
                server = verifier.getVerifiedAddress();
            }
            if(intVerifier.verify(this.getData(GeneralDataKeysEnum.HEARTBEATBROADCAST))) {
                queryPort = intVerifier.getVerifiedValue();
            }
        } else if(this.isQueryAnswer()) { return sender; }
        if(server != null && queryPort != 0) { return new InetSocketAddress(server, queryPort); }
        return null;
    }

    public GameBase getGame() {
        return GameBase.getGameForGameName(this.getData(GeneralDataKeysEnum.GAMENAME));
    }

    @Override
    public MessageData combine(MessageData combineable) {
        MessageData combination = new MessageData();
        combination.map.putAll(combineable.map);
        combination.map.putAll(this.map);
        return combination;
    }

    @Override
    public void update(MessageData combineable) {
        this.map.putAll(combineable.map);
    }

    public QueryInfo getQueryInfo() {
        if(this.isQueryAnswer()) {
            QueryIdFormatVerify verifier = new QueryIdFormatVerify();
            QueryInfo info = null;
            if(verifier.verify(this.getData(GeneralDataKeysEnum.QUERYID))) {
                info = verifier.getVerifiedQueryInfo();
                if(this.containsKey(GeneralDataKeysEnum.FINAL)) {
                    info.setFinal();
                }
                return info;
            }
        }
        return null;
    }
}
