package de.poweruser.powerserver.commands;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class HelpCommand extends CommandBase {

    public HelpCommand(String commandString) {
        super(commandString);
    }

    @Override
    public boolean handle(String[] arguments) {
        return false;
    }

    @Override
    public void showCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Information about this application:\n\n");
        sb.append(" - PowerServer - \n");
        sb.append("PowerServer is a master server application for games that had used the GameSpy service\n");
        sb.append("The currently supported Games are:\n\n");
        sb.append("GAME NAME  [Identification key]\n");

        sb.append("Operation Flashpoint:Resistance  [opflashr]\n");
        sb.append("Arma: Cold War Assault  [opflashr]\n\n");
        sb.append("This application is open-source and the project's site is:  https://github.com/Poweruser/PowerServer/\n");
        sb.append("Developed by Poweruser - 2014\n\n");
        sb.append("Type 'commands' to see a list of available commands");
        Logger.logStatic(LogLevel.VERY_LOW, sb.toString());

    }
}
