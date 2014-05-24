package de.poweruser.powerserver.commands;

public abstract class CommandBase implements CommandInterface {

    private final String commandString;

    public CommandBase(String commandString) {
        this.commandString = commandString;
    }

    public String getCommandString() {
        return this.commandString;
    }
}
