package de.poweruser.powerserver.commands;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.PowerServer;
import de.poweruser.powerserver.main.parser.dataverification.IPAddressVerify;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public class AddServerCommand extends CommandBase {

    private PowerServer server;

    public AddServerCommand(String commandString, PowerServer server) {
        super(commandString);
        this.server = server;
    }

    @Override
    public boolean handle(String[] arguments) {
        if(arguments.length == 2) {
            String gamename = arguments[0];
            GameBase game = GameBase.getGameForGameName(gamename);
            if(game != null) {
                String[] address = arguments[1].split(":");
                if(address.length == 2) {
                    IPAddressVerify ipverify = new IPAddressVerify();
                    IntVerify portverify = new IntVerify(0, 65535);
                    if(ipverify.verify(address[0]) && portverify.verify(address[1])) {
                        this.server.addServer(game, ipverify.getVerifiedAddress(), portverify.getVerifiedValue());
                        return true;
                    }
                }
                Logger.logStatic(LogLevel.VERY_LOW, "The entered server address (" + arguments[1] + ") is invalid.");
            } else {
                Logger.logStatic(LogLevel.VERY_LOW, "The game \"" + gamename + "\" was not recognized.");
            }
        }
        return false;
    }

    @Override
    public void showCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Command - addserver:\n\n");
        sb.append("Syntax:  addserver <game id-key> <Address:QueryPort>\n\n");
        sb.append("Type 'help' to see the list of available games and their game id-keys (Identification keys)\n");
        Logger.logStatic(LogLevel.VERY_LOW, sb.toString());
    }
}
