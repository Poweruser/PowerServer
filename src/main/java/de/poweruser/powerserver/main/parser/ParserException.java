package de.poweruser.powerserver.main.parser;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.network.UDPMessage;

public class ParserException extends Throwable {
    private static final long serialVersionUID = 4057281636598893525L;
    private UDPMessage message;
    private GameBase game;
    private String errorMessage;

    public ParserException(String errorMessage, UDPMessage message, GameBase game) {
        this.errorMessage = errorMessage;
        this.message = message;
        this.game = game;
    }

    public UDPMessage getUDPMessage() {
        return this.message;
    }

    public GameBase getGame() {
        return this.game;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
