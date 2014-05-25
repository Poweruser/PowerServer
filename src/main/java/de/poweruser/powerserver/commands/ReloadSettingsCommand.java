package de.poweruser.powerserver.commands;

import de.poweruser.powerserver.main.PowerServer;

public class ReloadSettingsCommand extends CommandBase {

    private PowerServer server;

    public ReloadSettingsCommand(String commandString, PowerServer server) {
        super(commandString);
        this.server = server;
    }

    @Override
    public boolean handle(String[] arguments) {
        this.server.reloadSettingsFile();
        return true;
    }

    @Override
    public void showCommandHelp() {

    }

}
