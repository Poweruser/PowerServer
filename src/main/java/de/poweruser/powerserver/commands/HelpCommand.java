package de.poweruser.powerserver.commands;

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
        // TODO Auto-generated method stub

    }
}
