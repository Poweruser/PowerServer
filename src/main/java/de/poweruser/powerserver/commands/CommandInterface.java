package de.poweruser.powerserver.commands;

public interface CommandInterface {

    /**
     * This method is called by the CommandRegistry, and executes the command.
     * Before the command does any change, the parsed arguments shall be checked
     * first. When these do not fit the command handle shall return false.
     * If all is valid, and the command was executed fine, return true.
     * 
     * @param arguments
     *            the arguments of the command, that the user had entered. The
     *            command string which identified this command is not included.
     *            If the array has a length of 0, no arguments were passed.
     * 
     * @return If an error occurred and the command help shall be shown false,
     *         otherwise true
     */

    public boolean handle(String[] arguments);

    /**
     * Generates a log message for the command. This method is called by the
     * CommandRegistry, when the handle method had returned false
     * 
     */

    public void showCommandHelp();

}
