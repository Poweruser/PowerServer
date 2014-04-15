package de.poweruser.powerserver.main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.parser.GamespyProtocol1Parser;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.network.UDPManager;
import de.poweruser.powerserver.network.UDPMessage;
import de.poweruser.powerserver.settings.Settings;

public class PowerServer extends Observable {

    private Map<GameBase, ServerList> serverLists;
    private GamespyProtocol1Parser gsp1Parser;
    private UDPManager udpManager;
    private Object waitObject = new Object();
    private boolean running;
    private Logger logger;
    private Settings settings;
    private List<InetAddress> masterServers;
    private long lastMasterServerLookup;

    public PowerServer() throws IOException {
        this.logger = new Logger(new File("server.log"));
        this.settings = new Settings(new File("settings.cfg"));
        this.reloadSettingsFile();
        this.gsp1Parser = new GamespyProtocol1Parser();
        this.serverLists = new HashMap<GameBase, ServerList>();
        this.mainloop();
    }

    private void reloadSettingsFile() throws SocketException {
        this.settings.load();
        this.lookUpAndGetMasterServerList();
        this.updateSupportedGames();
        this.setupUDPSocket();
    }

    private void setupUDPSocket() throws SocketException {
        int udpport = this.settings.getUDPPort();
        if(udpport >= 1024 && udpport <= 65535) {
            if(this.udpManager != null && this.udpManager.getPort() != udpport) {
                UDPManager newUDPManager = null;
                try {
                    newUDPManager = new UDPManager(udpport);
                } catch(SocketException e) {
                    this.logger.log("Could establish the UDPManager on port " + udpport + ". " + e.getMessage());
                }
                if(newUDPManager != null) {
                    this.udpManager.shutdown();
                    this.udpManager = newUDPManager;
                }
            }
            if(this.udpManager == null) {
                this.udpManager = new UDPManager(udpport);
            }
        } else {
            throw new IllegalArgumentException("The UDP port number must be an integer between 1024 and 65535");
        }
    }

    private void updateSupportedGames() {
        List<String> configGames = this.settings.getSupportedGamesList();
        List<GameBase> gameList = new ArrayList<GameBase>();
        for(String gamename: configGames) {
            GameBase game = GameBase.getGameForGameName(gamename);
            if(game != null) {
                gameList.add(game);
            } else {
                this.logger.log("Game \"" + gamename + "\" from settings file not recognized");
            }
        }
        Iterator<GameBase> iter = this.serverLists.keySet().iterator();
        while(iter.hasNext()) {
            GameBase game = iter.next();
            if(!gameList.contains(game)) {
                iter.remove();
            } else {
                gameList.remove(game);
            }
        }
        for(GameBase game: gameList) {
            this.serverLists.put(game, new ServerList(game));
        }
    }

    private void lookUpAndGetMasterServerList() {
        this.masterServers = this.settings.getMasterServerList();
        this.lastMasterServerLookup = System.currentTimeMillis();
    }

    private void mainloop() {
        this.running = true;
        while(this.running) {
            if(this.udpManager == null || !this.udpManager.hasMessages()) {
                synchronized(this.waitObject) {
                    try {
                        this.waitObject.wait(5000);
                    } catch(InterruptedException e) {}
                }
                if(this.isLastMasterServerLookupDue(60000L + 60L)) {
                    this.lookUpAndGetMasterServerList();
                }
            } else {
                while(this.udpManager.hasMessages()) {
                    this.handleIncomingMessage(this.udpManager.takeFirstMessage());
                }
            }
        }
    }

    private void handleIncomingMessage(UDPMessage message) {
        if(message == null) { return; }
        MessageData data = null;
        try {
            data = this.gsp1Parser.parse(null, message);
        } catch(ParserException e) {
            this.logger.log(e.getErrorMessage() + "\nReceived data: " + message.toString());
        }
        if(data != null) {
            GameBase game = data.getGame();
            if(game != null) {
                ServerList list = this.serverLists.get(game);
                InetSocketAddress sender = message.getSender();
                if(data.isHeartBeat()) {
                    list.incomingHeartBeat(sender, data);
                } else if(data.isHeartBeatBroadcast()) {
                    if(!this.masterServers.contains(sender.getAddress())) {
                        if(this.isLastMasterServerLookupDue(60000L * 5L)) {
                            this.lookUpAndGetMasterServerList();
                        }
                    }
                    if(this.masterServers.contains(sender.getAddress())) {
                        list.incomingHeartBeatBroadcast(sender.getAddress(), data);
                    } else {
                        this.logger.log("Got a heartbeat broadcast from " + sender.toString() + " which is not listed as a master server! Message: " + message.toString());
                    }
                } else {
                    list.incomingQueryAnswer(sender, data);
                }
            } else {
                this.logger.log("Couldnt find corresponding game for message: " + message.toString());
            }
        }
    }

    public void shutdown() {
        this.running = false;
    }

    private boolean isLastMasterServerLookupDue(long timeDiff) {
        return (System.currentTimeMillis() - this.lastMasterServerLookup) > timeDiff;
    }
}
