package de.poweruser.powerserver.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.PowerServer;

public class BanIPCommand extends CommandBase {

    private PowerServer server;

    public BanIPCommand(String commandString, PowerServer server) {
        super(commandString);
        this.server = server;
    }

    @Override
    public boolean handle(String[] arguments) {
        if(arguments.length >= 1 && arguments.length <= 2) {
            String add = arguments[0];
            InetAddress address = null;
            try {
                address = InetAddress.getByName(add);
            } catch(UnknownHostException e) {
                Logger.logStatic(LogLevel.VERY_LOW, "The entered address " + add + " could not be resolved.", true);
                return false;
            }

            if(arguments.length == 2) {
                Date date = null;
                try {
                    date = this.parseBanDurationToDate(arguments[1]);
                } catch(IllegalArgumentException e) {
                    Logger.logStatic(LogLevel.VERY_LOW, "The entered duration is invalid, refer to the command help.", true);
                    return false;
                }
                if(date != null) {
                    boolean newBan = this.server.getBanManager().addTempBanByTimeStamp(address, date.getTime());
                    String expirationDate = this.server.getBanManager().getUnbanDate(address);
                    String message = "Temporary ban for " + address.toString() + (newBan ? " issued." : " updated.");
                    if(expirationDate != null) {
                        message += " Expires on: " + expirationDate;
                    }
                    Logger.logStatic(LogLevel.VERY_LOW, message, true);
                    return true;
                }
            } else {
                this.server.getBanManager().addPermBan(address);
                Logger.logStatic(LogLevel.VERY_LOW, "Permanent ban for " + address.toString() + " issued.", true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void showCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Command - banip:\n\n");
        sb.append("Syntax:  banip <Address> [time duration]\n\n");
        sb.append("The optional time duration is a combination of an integer followed by a time unit key.\n");
        sb.append("Multiple durations of different units can be combined, without spaces inbetween.\n");
        sb.append("Possible time unit keys are: M (months), w (weeks), d (days), h (hours), m (minutes), s (seconds)\n");
        sb.append("Examples: 3d (3 days), 2h30m (2 hours and 30 minutes), 1h20m10s (1 hour 20 minutes and 10 seconds)");
        Logger.logStatic(LogLevel.VERY_LOW, sb.toString());
    }

    public Date parseBanDurationToDate(String input) {
        Calendar cal = Calendar.getInstance();

        String currentNumber = "";
        for(int j = 0; j < input.length(); j++) {

            String c = input.substring(j, j + 1);
            try {
                Integer.parseInt(c);
                currentNumber += c;
                continue;
            } catch(Exception e) {}

            int num = Integer.parseInt(currentNumber);

            if(c.equals("w")) {
                cal.add(Calendar.WEEK_OF_YEAR, num);
            } else if(c.equals("d")) {
                cal.add(Calendar.DAY_OF_YEAR, num);
            } else if(c.equals("M")) {
                cal.add(Calendar.MONTH, num);
            } else if(c.equals("h")) {
                cal.add(Calendar.HOUR, num);
            } else if(c.equals("m")) {
                cal.add(Calendar.MINUTE, num);
            } else if(c.equals("s")) {
                cal.add(Calendar.SECOND, num);
            } else {
                throw new IllegalArgumentException("Invalid time measurement: " + c);
            }
            currentNumber = "";
        }
        return cal.getTime();
    }
}
