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
        sb.append("setloglevel <#>      Sets the level of logging ");
        for(LogLevel l: LogLevel.values()) {
            sb.append(l.getValue());
            sb.append("(");
            sb.append(l.toString());
            sb.append(") ");
        }
        sb.append("\n");
        sb.append("commands       Shows this list of available commands\n");
        sb.append("help                    Displays some general information about this application");
        Logger.logStatic(LogLevel.VERY_LOW, sb.toString());
    }

}
