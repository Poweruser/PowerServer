package de.poweruser.powerserver.games;

import de.poweruser.powerserver.main.MessageData;

public interface GameServerInterface {
    public void processNewMessage(MessageData message);
}
