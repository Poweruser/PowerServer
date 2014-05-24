package de.poweruser.powerserver.commands;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public class LogLevelCommand extends CommandBase {

    public LogLevelCommand(String commandString) {
        super(commandString);
    }

    @Override
    public boolean handle(String[] arguments) {
        if(arguments.length == 1) {
            IntVerify v = new IntVerify(0, LogLevel.getMaxLevel().getValue());
            if(v.verify(arguments[0])) {
                int value = v.getVerifiedValue();
                Logger.setLogLevel(value);
                Logger.logStatic(LogLevel.VERY_LOW, "Log level set to " + LogLevel.valueToLevel(value).toString() + " (" + value + ")");
                return true;
            } else {
                boolean found = false;
                for(LogLevel l: LogLevel.values()) {
                    if(l.toString().equalsIgnoreCase(arguments[0])) {
                        Logger.setLogLevel(l.getValue());
                        Logger.logStatic(LogLevel.VERY_LOW, "Log level set to " + l.toString() + " (" + l.getValue() + ")");
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    Logger.logStatic(LogLevel.VERY_LOW, "Invalid argument specified");
                }
            }
        } else if(arguments.length > 1) {
            Logger.logStatic(LogLevel.VERY_LOW, "Invalid arguments specified");
        }
        return false;
    }

    @Override
    public void showCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Command - loglevel:\n\n");
        sb.append("Syntax:  loglevel <#>\n\n");
        sb.append(" #   The level of logging, that you want to set. Available levels are 0 through 4:\n");
        sb.append("     0  -  Very low: Only the most important events are logged\n");
        sb.append("     1  -  Low: Major errors that dont interrupt the server operation though are logged as well\n");
        sb.append("     2  -  Normal: Relevant information on the server events are logged as well\n");
        sb.append("     3  -  High: Minor errors that usually can be ignored are logged as well\n");
        sb.append("     4  -  Very high: Logs all kinds of error and events");
        Logger.logStatic(LogLevel.VERY_LOW, sb.toString());

    }
}
