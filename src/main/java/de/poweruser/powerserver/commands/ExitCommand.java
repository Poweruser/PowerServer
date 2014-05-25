package de.poweruser.powerserver.commands;

import de.poweruser.powerserver.main.PowerServer;

public class ExitCommand extends CommandBase {

    private PowerServer server;

    public ExitCommand(String commandString, PowerServer server) {
        super(commandString);
        this.server = server;
    }

    @Override
    public boolean handle(String[] arguments) {
        this.server.shutdown();
        return true;
    }

    @Override
    public void showCommandHelp() {

    }

}
