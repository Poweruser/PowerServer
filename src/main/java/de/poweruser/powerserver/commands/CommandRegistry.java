package de.poweruser.powerserver.commands;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class CommandRegistry {

    private Map<String, CommandInterface> commandMap;
    private ArrayDeque<String> commandQueue;

    public CommandRegistry() {
        this.commandMap = new HashMap<String, CommandInterface>();
        this.commandQueue = new ArrayDeque<String>();
    }

    /**
     * Registers the parsed command in the CommandRegistry. If a
     * command with the same command string was already registered,
     * that one is replaced
     * 
     * @param command
     *            The command that shall be registered
     */

    public void register(CommandBase command) {
        this.commandMap.put(command.getCommandString(), command);
    }

    /**
     * Takes the first element from the commandQueue and executes the one
     * registered command, that matches the first word of the entered command.
     * If no matching command was found, a log message is printed.
     * This method shall not be run from a different thread than the main
     * thread, unless thread-safety, on the things that the commands do, is
     * guaranteed
     */

    public void issueNextQueuedCommand() {
        String line = this.commandQueue.pollFirst();
        if(line == null || line.trim().isEmpty()) { return; };
        String formated = line.trim().replaceAll("\\s+", " ");
        String[] items = formated.split(" ");
        String[] arguments = new String[0];
        if(items.length > 1) {
            arguments = new String[items.length - 1];
            System.arraycopy(items, 1, arguments, 0, items.length - 1);
        }
        String commandString = items[0];
        if(this.commandMap.containsKey(commandString)) {
            Logger.logStatic(LogLevel.VERY_LOW, "Entered command: '" + formated + "'", true);
            CommandInterface com = this.commandMap.get(commandString);
            if(!com.handle(arguments)) {
                com.showCommandHelp();
            }
        } else {
            Logger.logStatic(LogLevel.VERY_LOW, "Unknown command '" + commandString + "'. Type 'commands' to get a list of all available commands.", true);
        }
    }

    /**
     * Adds a command as a String in the exact same way, that the user had
     * entered it to the command queue.
     * 
     * @param command
     *            The command that the user had entered
     */

    public void queueCommand(String command) {
        this.commandQueue.addLast(command);
    }
}
