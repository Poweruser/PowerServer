package de.poweruser.powerserver.main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.commands.CommandRegistry;
import de.poweruser.powerserver.commands.CommandsCommand;
import de.poweruser.powerserver.commands.HelpCommand;
import de.poweruser.powerserver.commands.LogLevelCommand;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GamesEnum;
import de.poweruser.powerserver.gamespy.EncType;
import de.poweruser.powerserver.logger.LogLevel;
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
    private CommandRegistry commandReg;

    public static final int MASTERSERVER_UDP_PORT = 27900;
    public static final int MASTERSERVER_TCP_PORT = 28900;

    public PowerServer() throws IOException {
        for(GamesEnum g: GamesEnum.values()) {}
        for(EncType enc: EncType.values()) {}
        this.commandReg = new CommandRegistry();
        this.commandReg.register(new HelpCommand("help"));
        this.commandReg.register(new LogLevelCommand("loglevel"));
        this.commandReg.register(new CommandsCommand("commands"));
        this.running = false;
        this.settings = new Settings(new File("settings.cfg"));
        this.supportedGames = new HashSet<GameBase>();
        this.udpManager = new UDPManager(MASTERSERVER_UDP_PORT);
        this.tcpManager = new TCPManager(MASTERSERVER_TCP_PORT);
        this.reloadSettingsFile();
        this.gsp1Parser = new GamespyProtocol1Parser();
    }

    private void reloadSettingsFile() throws IOException {
        this.settings.load();
        this.lookUpAndGetMasterServerList(true);
        this.updateSupportedGames();
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
                Logger.logStatic(LogLevel.LOW, "Game \"" + gamename + "\" from settings file not recognized");
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
            if(this.udpManager.isSocketClosed()) {
                this.udpManager.shutdown();
                try {
                    this.udpManager = new UDPManager(MASTERSERVER_UDP_PORT);
                } catch(SocketException e) {
                    Logger.logStackTraceStatic(LogLevel.VERY_LOW, "The Socket of the UDPManager was closed and setting up a new UDPManager raised an exception: " + e.toString(), e);
                    this.running = false;
                }
            } else {
                while(this.udpManager.hasMessages()) {
                    this.handleIncomingMessage(this.udpManager.takeFirstMessage());
                }
                for(GameBase game: this.supportedGames) {
                    ServerList list = game.getServerList();
                    List<InetSocketAddress> toQuery = list.checkForServersToQueryAndOutdatedServers();
                    if(toQuery != null) {
                        for(InetSocketAddress i: toQuery) {
                            list.queryServer(i, this.udpManager.getUDPSender(), false);
                        }
                    }
                }
                this.udpManager.getUDPSender().flush();
            }
            if(this.tcpManager.isSocketClosed()) {
                this.tcpManager.shutdown();
                try {
                    this.tcpManager = new TCPManager(MASTERSERVER_TCP_PORT);
                } catch(IOException e) {
                    Logger.logStackTraceStatic(LogLevel.VERY_LOW, "The Socket of the TCPManager was closed and setting up a new TCPManager raised an exception: " + e.toString(), e);
                    this.running = false;
                }
            } else {
                this.tcpManager.processConnections();
            }
            synchronized(this.waitObject) {
                try {
                    this.waitObject.wait(100);
                } catch(InterruptedException e) {}
            }
            if(this.isLastMasterServerLookupDue(true, this.settings.getListsDownloadInterval(TimeUnit.HOURS), TimeUnit.HOURS)) {
                Logger.logStatic(LogLevel.HIGH, "Updating the master server list (Download of the domains and refreshing of the IPs)");
                this.lookUpAndGetMasterServerList(true);
            }
        }
    }

    private void handleIncomingMessage(UDPMessage message) {
        if(message == null) { return; }
        MessageData data = null;
        try {
            data = this.gsp1Parser.parse(null, message);
        } catch(ParserException e) {
            Logger.logStatic(LogLevel.HIGH, "Error while parsing an incoming udpmessage:");
            Logger.log(LogLevel.HIGH, e);
        }
        if(data != null) {
            GameBase game = data.getGame();
            if(game == null) {
                Logger.logStatic(LogLevel.HIGH, "Couldnt find corresponding game for message: " + message.toString());
            } else if(!this.isGameSupported(game)) {
                Logger.logStatic(LogLevel.HIGH, "Got an incoming message for an unsupported game: " + message.toString());
            } else {
                ServerList list = game.getServerList();
                InetSocketAddress sender = message.getSender();
                InetSocketAddress server = data.constructQuerySocketAddress(sender);
                if(server != null) {
                    UDPSender udpSender = this.udpManager.getUDPSender();
                    if(data.isHeartBeat()) {
                        boolean firstHeartBeat = list.incomingHeartBeat(server, data);
                        udpSender.queueHeartBeatBroadcast(masterServers, game.createHeartbeatBroadcast(server, data));
                        if(firstHeartBeat || data.hasStateChanged()) {
                            list.queryServer(server, udpSender, false);
                        }
                    } else if(data.isHeartBeatBroadcast()) {
                        if(!this.masterServers.contains(sender.getAddress())) {
                            if(this.isLastMasterServerLookupDue(false, 5L, TimeUnit.MINUTES)) {
                                this.lookUpAndGetMasterServerList(false);
                            }
                        }
                        if(this.masterServers.contains(sender.getAddress())) {
                            boolean firstHeartBeat = list.incomingHeartBeatBroadcast(server, data);
                            if((firstHeartBeat || data.hasStateChanged()) && list.isBroadcastedServer(server)) {
                                list.queryServer(server, udpSender, false);
                            }
                        } else {
                            Logger.logStatic(LogLevel.NORMAL, "Got a heartbeat broadcast from " + sender.toString() + " which is not listed as a master server! Message: " + message.toString());
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

    public void issueCommand(String command) {
        this.commandReg.issueCommand(command);
    }
}
