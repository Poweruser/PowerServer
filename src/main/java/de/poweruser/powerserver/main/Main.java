package de.poweruser.powerserver.main;

import java.io.IOException;

import de.poweruser.powerserver.logger.Logger;

public class Main {

    public static void main(String[] args) {
        PowerServer server = null;
        Logger.logStatic("Starting the master server ...");
        try {
            server = new PowerServer();
        } catch(IOException e) {
            Logger.logStackTraceStatic("Failed to set up the server: " + e.toString(), e);
        }
        if(server != null) {
            try {
                server.mainloop();
                Logger.logStatic("Shutting down the master server ...");
            } catch(Exception e) {
                Logger.logStackTraceStatic("The server quit unexpectedly with an exception of type: " + e.toString(), e);
            }
        }
    }
}
