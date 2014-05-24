package de.poweruser.powerserver.commands;

public interface CommandInterface {

    public boolean handle(String[] arguments);

    public void showCommandHelp();

}
