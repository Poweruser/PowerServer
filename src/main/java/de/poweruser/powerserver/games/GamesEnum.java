package de.poweruser.powerserver.games;

import de.poweruser.powerserver.games.opflashr.OperationFlashpointResistance;
import de.poweruser.powerserver.main.parser.GamespyProtocol1Parser;

public enum GamesEnum {
    OPERATIONFLASHPOINT_RESISTANCE(new OperationFlashpointResistance("opflashr", "Y3k7x1", new GamespyProtocol1Parser()));

    private GameBase game;

    private GamesEnum(GameBase game) {
        this.game = game;
    }

    public GameBase getGame() {
        return this.game;
    }
}
