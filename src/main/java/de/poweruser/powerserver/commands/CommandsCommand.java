package de.poweruser.powerserver.commands;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class CommandsCommand extends CommandBase {

    public CommandsCommand(String commandString) {
        super(commandString);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean handle(String[] arguments) {
        return false;
    }

    @Override
    public void showCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Overview over all available commands:\n\n");
        sb.append("setloglevel <#>\t\tSets the level of logging. Values:  ");
        for(LogLevel l: LogLevel.values()) {
            sb.append(l.getValue());
            sb.append("(");
            sb.append(l.toString());
            sb.append(") ");
        }
        sb.append("\n");
        sb.append("reload    \t\tReloads the settings file\n");
        sb.append("addserver <game id-key> <Address:QueryPort>\tAdds a server to a game's server list\n");
        sb.append("commands\t\tShows this list of available commands\n");
        sb.append("help    \t\tDisplays some general information about this application\n");
        sb.append("exit    \t\tShuts down the server. Aliases: quit, shutdown, stop, end");
        Logger.logStatic(LogLevel.VERY_LOW, sb.toString(), true);
    }

}
