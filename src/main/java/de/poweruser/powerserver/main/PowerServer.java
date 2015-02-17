package de.poweruser.powerserver.main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.commands.AddServerCommand;
import de.poweruser.powerserver.commands.BanIPCommand;
import de.poweruser.powerserver.commands.CommandRegistry;
import de.poweruser.powerserver.commands.CommandsCommand;
import de.poweruser.powerserver.commands.ExitCommand;
import de.poweruser.powerserver.commands.HelpCommand;
import de.poweruser.powerserver.commands.LogLevelCommand;
import de.poweruser.powerserver.commands.ReloadSettingsCommand;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GamesEnum;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.gamespy.EncType;
import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.parser.GamespyProtocol1Parser;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.main.security.BanManager;
import de.poweruser.powerserver.network.TCPManager;
import de.poweruser.powerserver.network.UDPManager;
import de.poweruser.powerserver.network.UDPMessage;
import de.poweruser.powerserver.network.UDPSender;
import de.poweruser.powerserver.settings.Settings;

public class PowerServer {

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
    private BanManager<InetAddress> banManager;

    public static final String VERSION = "1.2";

    public static final int MASTERSERVER_UDP_PORT = 27900;
    public static final int MASTERSERVER_TCP_PORT = 28900;

    public PowerServer(BanManager<InetAddress> banManager) throws IOException {
        this.banManager = banManager;
        this.settings = new Settings(new File("settings.cfg"));
        for(GamesEnum g: GamesEnum.values()) {
            g.getGame().setSettings(this.settings);
        }
        for(@SuppressWarnings("unused")
        EncType enc: EncType.values()) {}
        this.commandReg = new CommandRegistry();
        this.commandReg.register(new HelpCommand("help"));
        this.commandReg.register(new LogLevelCommand("setloglevel"));
        this.commandReg.register(new CommandsCommand("commands"));
        this.commandReg.register(new ReloadSettingsCommand("reload", this));
        this.commandReg.register(new AddServerCommand("addserver", this));
        this.commandReg.register(new BanIPCommand("ban", this));
        String[] exitAliases = new String[] { "exit", "stop", "quit", "shutdown", "end" };
        for(String str: exitAliases) {
            this.commandReg.register(new ExitCommand(str, this));
        }
        this.running = false;
        this.supportedGames = new HashSet<GameBase>();
        this.udpManager = new UDPManager(MASTERSERVER_UDP_PORT, this.settings, this.banManager);
        this.tcpManager = new TCPManager(MASTERSERVER_TCP_PORT, this.settings, this.banManager);
        this.reloadSettingsFile();
        this.gsp1Parser = new GamespyProtocol1Parser();
    }

    public void reloadSettingsFile() {
        this.settings.load();
        if(this.settings.isPublicMode()) {
            this.lookUpAndGetMasterServerList(true);
        }
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
                    this.udpManager = new UDPManager(MASTERSERVER_UDP_PORT, this.settings, this.banManager);
                } catch(SocketException e) {
                    Logger.logStackTraceStatic(LogLevel.VERY_LOW, "The Socket of the UDPManager was closed and setting up a new UDPManager raised an exception: " + e.toString(), e);
                    this.running = false;
                }
            } else {
                int messageCount = 0;
                while(this.udpManager.hasMessages() && messageCount++ < UDPManager.MAX_MESSAGECOUNT_PER_CYCLE) {
                    this.handleIncomingMessage(this.udpManager.takeFirstMessage());
                }
                for(GameBase game: this.supportedGames) {
                    ServerList list = game.getServerList();
                    List<InetSocketAddress> toQuery = list.checkForServersToQueryAndOutdatedServers(this.settings);
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
                    this.tcpManager = new TCPManager(MASTERSERVER_TCP_PORT, this.settings, this.banManager);
                } catch(IOException e) {
                    Logger.logStackTraceStatic(LogLevel.VERY_LOW, "The Socket of the TCPManager was closed and setting up a new TCPManager raised an exception: " + e.toString(), e);
                    this.running = false;
                }
            } else {
                this.tcpManager.processConnections();
            }
            this.commandReg.issueNextQueuedCommand();
            if(this.banManager.hasChanged()) {
                this.banManager.saveBanListToFile();
            }
            synchronized(this.waitObject) {
                try {
                    this.waitObject.wait(100);
                } catch(InterruptedException e) {}
            }
            if(this.settings.isPublicMode() && this.isLastMasterServerLookupDue(true, this.settings.getListsDownloadInterval(TimeUnit.MINUTES), TimeUnit.MINUTES)) {
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
        this.gsp1Parser.reset();

        if(data != null) {
            GameBase game = data.getGame();
            if(game == null && !data.isQueryAnswer()) {
                Logger.logStatic(LogLevel.HIGH, "Couldnt find corresponding game for message: " + message.toString());
            } else if(game != null && !this.isGameSupported(game)) {
                Logger.logStatic(LogLevel.HIGH, "Got an incoming message for an unsupported game: " + message.toString());
            } else {
                InetSocketAddress sender = message.getSender();
                InetSocketAddress server = data.constructQuerySocketAddress(sender);
                this.handleIncomingMessage(sender, server, data, message);
            }
        }
    }

    private String getUDPMessageString(String prefix, UDPMessage message) {
        if(message == null) { return ""; }
        return prefix + message.toString();
    }

    private void handleIncomingMessage(InetSocketAddress sender, InetSocketAddress server, MessageData data, UDPMessage message) {
        if(data == null) { return; }

        boolean manuallyAdded = message == null;
        GameBase game = data.getGame();
        if(game == null && !data.isQueryAnswer()) {
            Logger.logStatic(LogLevel.HIGH, "Couldnt find corresponding game" + this.getUDPMessageString(" for message: ", message));
        } else if(game != null && !this.isGameSupported(game)) {
            Logger.logStatic(LogLevel.HIGH, "Got an incoming message for an unsupported game" + this.getUDPMessageString(": ", message));
        } else {
            ServerList list = null;
            if(game != null) {
                list = game.getServerList();
            } else {
                for(GameBase gb: this.supportedGames) {
                    ServerList serverList = gb.getServerList();
                    if(serverList.hasServer(server)) {
                        list = serverList;
                        break;
                    }
                }
            }
            if(server != null && list != null) {
                UDPSender udpSender = this.udpManager.getUDPSender();
                if(data.isHeartBeat()) {
                    boolean firstHeartBeat = list.incomingHeartBeat(server, data, manuallyAdded);
                    if(this.settings.isPublicMode()) {
                        udpSender.queueHeartBeatBroadcast(masterServers, game.createHeartbeatBroadcast(server, data));
                    }
                    if(firstHeartBeat || (data.hasStateChanged() && this.settings.getQueryServersOnHeartbeat())) {
                        list.queryServer(server, udpSender, false);
                    }
                } else if(data.isHeartBeatBroadcast()) {
                    if(this.settings.isPublicMode()) {
                        if(!this.masterServers.contains(sender.getAddress())) {
                            if(this.isLastMasterServerLookupDue(false, 5L, TimeUnit.MINUTES)) {
                                this.lookUpAndGetMasterServerList(false);
                            }
                        }
                        if(this.masterServers.contains(sender.getAddress())) {
                            boolean firstHeartBeat = list.incomingHeartBeatBroadcast(server, data);
                            if((firstHeartBeat || (data.hasStateChanged() && this.settings.getQueryServersOnHeartbeat())) && list.isBroadcastedServer(server)) {
                                list.queryServer(server, udpSender, false);
                            }
                        } else {
                            Logger.logStatic(LogLevel.NORMAL, "Got a heartbeat broadcast from " + sender.toString() + " which is not listed as a master server!" + this.getUDPMessageString(" Message: ", message));
                        }
                    }
                } else if(data.isQueryAnswer()) {
                    if(list.incomingQueryAnswer(server, data) && this.settings.isPublicMode()) {
                        udpSender.queueHeartBeatBroadcast(masterServers, game.createHeartbeatBroadcast(server, data));
                    }
                } else {
                    Logger.logStatic(LogLevel.HIGH, "Received a UDPMessage from " + sender.getAddress().toString() + " that could not be recognised as either a heartbeat, a heartbeat broadcast or a query answer." + this.getUDPMessageString(" Message: ", message));
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

    public void queueCommand(String command) {
        this.commandReg.queueCommand(command);
    }

    public String getVersion() {
        return VERSION;
    }

    public void addServer(GameBase game, InetAddress address, int port) {
        if(game != null && this.supportedGames.contains(game)) {
            InetSocketAddress serverAddress = new InetSocketAddress(address, port);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(GeneralDataKeysEnum.HEARTBEAT.getKeyString(), String.valueOf(port));
            map.put(GeneralDataKeysEnum.GAMENAME.getKeyString(), game.getGameName());
            map.put(GeneralDataKeysEnum.STATECHANGED.getKeyString(), String.valueOf(1));
            MessageData data = new MessageData(map);
            this.handleIncomingMessage(serverAddress, serverAddress, data, null);
        } else {
            String gamename = (game == null ? "null" : game.getGameName());
            Logger.logStatic(LogLevel.LOW, "Error while trying to add the server to the list: The passed game \"" + gamename + "\" is not supported.");
        }
    }

    public BanManager<InetAddress> getBanManager() {
        return this.banManager;
    }
}
