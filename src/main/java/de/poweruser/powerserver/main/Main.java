package de.poweruser.powerserver.main;

import java.io.File;
import java.io.IOException;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.gui.MainWindow;
import de.poweruser.powerserver.main.security.SecurityAndBanManager;

public class Main {

    public static void main(String[] args) throws IOException {
        boolean gui = true;
        for(String s: args) {
            if(s.equalsIgnoreCase("-nogui")) {
                gui = false;
            }
        }
        File logFile = new File("server.log");
        Logger logger = new Logger(logFile);

        MainWindow m = null;
        if(gui) {
            m = new MainWindow();
            m.setVisible(true);
        } else {
            logger.log(LogLevel.VERY_LOW, "Operating in console mode. Check the log file " + logFile.getAbsolutePath() + " for a more detailed log", true);
        }
        File policyFile = new File("PowerServer.policy");
        SecurityAndBanManager secManager = new SecurityAndBanManager();
        if(policyFile.exists()) {
            System.setProperty("java.security.policy", policyFile.getName());
            System.setSecurityManager(secManager);
            Logger.logStatic(LogLevel.VERY_LOW, "SecurityManager enabled.");
        } else {
            Logger.logStatic(LogLevel.VERY_LOW, "SecurityManager not enabled. The policy file \"PowerServer.policy\" is missing.");
        }
        PowerServer server = null;
        ConsoleReader reader = null;
        secManager.loadBanListFromFile();
        logger.log(LogLevel.VERY_LOW, "Starting the master server ...", true);
        try {
            server = new PowerServer(secManager);
            if(m != null) {
                m.setModel(server);
            } else {
                reader = new ConsoleReader(server);
            }
        } catch(IOException e) {
            Logger.logStackTraceStatic(LogLevel.VERY_LOW, "Failed to set up the server: " + e.toString(), e, true);
        }
        boolean crash = false;
        if(server != null) {
            try {
                server.mainloop();
                logger.log(LogLevel.VERY_LOW, "Shutting down the master server ...", true);
            } catch(Exception e) {
                crash = true;
                Logger.logStackTraceStatic(LogLevel.VERY_LOW, "The server quit unexpectedly with an exception of type: " + e.toString(), e, true);
            }
            server.shutdown();
        }
        if(m != null && !crash) {
            m.shutdown();
        }
        if(reader != null) {
            reader.shutdown();
        }
    }
}
