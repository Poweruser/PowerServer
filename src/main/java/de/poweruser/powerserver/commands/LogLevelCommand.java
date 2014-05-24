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
                for(LogLevel l: LogLevel.values()) {
                    if(l.toString().equalsIgnoreCase(arguments[0])) {
                        Logger.setLogLevel(l.getValue());
                        Logger.logStatic(LogLevel.VERY_LOW, "Log level set to " + l.toString() + " (" + l.getValue() + ")");
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void showCommandHelp() {
        // TODO Auto-generated method stub

    }

}
