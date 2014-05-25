package de.poweruser.powerserver.main;

import java.io.File;
import java.io.IOException;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.gui.MainWindow;

public class Main {

    public static void main(String[] args) throws IOException {
        boolean gui = true;
        for(String s: args) {
            if(s.equalsIgnoreCase("-nogui")) {
                gui = false;
            }
        }
        Logger logger = new Logger(new File("server.log"));
        MainWindow m = null;
        if(gui) {
            m = new MainWindow();
            m.setVisible(true);
        }
        PowerServer server = null;
        logger.log(LogLevel.VERY_LOW, "Starting the master server ...");
        try {
            server = new PowerServer();
            if(m != null) {
                m.setModel(server);
            }
        } catch(IOException e) {
            Logger.logStackTraceStatic(LogLevel.VERY_LOW, "Failed to set up the server: " + e.toString(), e);
        }
        if(server != null) {
            try {
                server.mainloop();
                logger.log(LogLevel.VERY_LOW, "Shutting down the master server ...");
            } catch(Exception e) {
                Logger.logStackTraceStatic(LogLevel.VERY_LOW, "The server quit unexpectedly with an exception of type: " + e.toString(), e);
            }
            if(m != null) {
                server.deleteObserver(m);
            }
            server.shutdown();
        }
        if(m != null) {
            m.shutdown();
        }
    }
}
