package de.poweruser.powerserver.main;

import java.io.File;
import java.io.IOException;

import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.gui.MainWindow;

public class Main {

    public static void main(String[] args) throws IOException {
        boolean gui = false;
        for(String s: args) {
            if(s.equalsIgnoreCase("-gui")) {
                gui = true;
            }
        }
        MainWindow m = null;
        PowerServer server = null;
        Logger logger = new Logger(new File("server.log"));
        logger.log("Starting the master server ...");
        try {
            server = new PowerServer();
        } catch(IOException e) {
            Logger.logStackTraceStatic("Failed to set up the server: " + e.toString(), e);
        }
        if(server != null) {
            if(gui) {
                m = new MainWindow(server);
                m.setVisible(true);
                server.addObserver(m);
            }
            try {
                server.mainloop();
                logger.log("Shutting down the master server ...");
            } catch(Exception e) {
                Logger.logStackTraceStatic("The server quit unexpectedly with an exception of type: " + e.toString(), e);
            }
            server.shutdown();
        }
    }
}
