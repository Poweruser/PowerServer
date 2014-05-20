package de.poweruser.powerserver.main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GamesEnum;
import de.poweruser.powerserver.gamespy.EncType;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.parser.GamespyProtocol1Parser;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.network.TCPManager;
import de.poweruser.powerserver.network.UDPManager;
import de.poweruser.powerserver.network.UDPMessage;
import de.poweruser.powerserver.network.UDPSender;
import de.poweruser.powerserver.settings.Settings;

public class PowerServer extends Observable {

    private GamespyProtocol1Parser gsp1Parser;
    private UDPManager udpManager;
    private TCPManager tcpManager;
    private Object waitObject = new Object();
    private boolean running;
    private Settings settings;
    private List<InetAddress> masterServers;
    private long lastMasterServerRefresh;
    private long lastMasterServerDownload;
    private Set<GameBase> supportedGames;

    public static final int MASTERSERVER_UDP_PORT = 27900;
    public static final int MASTERSERVER_TCP_PORT = 28900;

    public PowerServer() throws IOException {
        for(GamesEnum g: GamesEnum.values()) {}
        for(EncType enc: EncType.values()) {}
        this.running = false;
        this.settings = new Settings(new File("settings.cfg"));
        this.supportedGames = new HashSet<GameBase>();
        this.reloadSettingsFile();
        this.gsp1Parser = new GamespyProtocol1Parser();
    }

    private void reloadSettingsFile() throws IOException {
        this.settings.load();
        this.lookUpAndGetMasterServerList(true);
        this.updateSupportedGames();
        this.setupNetwork();
    }

    private void setupNetwork() throws IOException {
        if(this.udpManager == null || this.udpManager.isShutdown()) {
            this.udpManager = new UDPManager(MASTERSERVER_UDP_PORT);
        }
        if(this.tcpManager == null || this.tcpManager.isShutdown()) {
            this.tcpManager = new TCPManager(MASTERSERVER_TCP_PORT);
        }
    }

    private boolean isGameSupported(GameBase game) {
        return this.supportedGames.contains(game);
    }

    private void updateSupportedGames() {
        List<String> configGames = this.settings.getSupportedGamesList();
        List<GameBase> gameList = new ArrayList<GameBase>();
        for(String gamename: configGames) {
            GameBase game = GameBase.getGameForGameName(gamename);
            if(game != null) {
                gameList.add(game);
            } else {
                Logger.logStatic("Game \"" + gamename + "\" from settings file not recognized");
            }
        }
        this.supportedGames.retainAll(gameList);
        this.supportedGames.addAll(gameList);
    }

    private void lookUpAndGetMasterServerList(boolean forceDownload) {
        this.masterServers = this.settings.getMasterServerList(forceDownload);
        if(forceDownload) {
            this.lastMasterServerDownload = System.currentTimeMillis();
        }
        this.lastMasterServerRefresh = System.currentTimeMillis();
    }

    public void mainloop() {
        if(this.running) { return; }
        this.running = true;
        while(this.running) {
            if(this.udpManager == null || !this.udpManager.hasMessages()) {
                synchronized(this.waitObject) {
                    try {
                        this.waitObject.wait(100);
                    } catch(InterruptedException e) {}
                }
                if(this.isLastMasterServerLookupDue(true, this.settings.getListsDownloadInterval(TimeUnit.HOURS), TimeUnit.HOURS)) {
                    this.lookUpAndGetMasterServerList(true);
                }
            } else {
                while(this.udpManager.hasMessages()) {
                    this.handleIncomingMessage(this.udpManager.takeFirstMessage());
                }
            }
            for(GameBase gb: this.supportedGames) {
                gb.getServerList().clearOutDatedServers();
            }
            if(this.tcpManager != null) {
                this.tcpManager.processConnections();
            }
        }
    }

    private void handleIncomingMessage(UDPMessage message) {
        if(message == null) { return; }
        MessageData data = null;
        try {
            data = this.gsp1Parser.parse(null, message);
        } catch(ParserException e) {
            Logger.logStatic("Error while parsing an incoming udpmessage:");
            Logger.log(e);
        }
        if(data != null) {
            GameBase game = data.getGame();
            if(game == null) {
                Logger.logStatic("Couldnt find corresponding game for message: " + message.toString());
            } else if(!this.isGameSupported(game)) {
                Logger.logStatic("Got an incoming message for an unsupported game: " + message.toString());
            } else {
                ServerList list = game.getServerList();
                InetSocketAddress sender = message.getSender();
                InetSocketAddress server = data.constructQuerySocketAddress(sender);
                if(server != null) {
                    UDPSender udpSender = this.udpManager.getUDPSender();
                    if(data.isHeartBeat()) {
                        list.incomingHeartBeat(server, data);
                        boolean stateChanged = data.hasStateChanged();
                        udpSender.broadcastHeartBeat(masterServers, game.createHeartbeatBroadcast(server, data));
                        if(stateChanged) {
                            udpSender.sendQuery(server, game.createStatusQuery(false));
                        }
                    } else if(data.isHeartBeatBroadcast()) {
                        if(!this.masterServers.contains(sender.getAddress())) {
                            if(this.isLastMasterServerLookupDue(false, 5L, TimeUnit.MINUTES)) {
                                this.lookUpAndGetMasterServerList(false);
                            }
                        }
                        if(this.masterServers.contains(sender.getAddress())) {
                            list.incomingHeartBeatBroadcast(server, data);
                            udpSender.sendQuery(server, game.createStatusQuery(false));
                        } else {
                            Logger.logStatic("Got a heartbeat broadcast from " + sender.toString() + " which is not listed as a master server! Message: " + message.toString());
                        }
                    } else {
                        list.incomingQueryAnswer(sender, data);
                    }
                }
            }
        }
    }

    public void shutdown() {
        this.running = false;
        this.udpManager.shutdown();
        this.tcpManager.shutdown();
    }

    private boolean isLastMasterServerLookupDue(boolean download, long timeDiff, TimeUnit inputUnit) {
        long time;
        if(download) {
            time = this.lastMasterServerDownload;
        } else {
            time = this.lastMasterServerRefresh;
        }
        return (System.currentTimeMillis() - time) > TimeUnit.MILLISECONDS.convert(timeDiff, inputUnit);
    }
}
