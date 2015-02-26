package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.exceptions.TooManyServersPerHostException;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerInterface;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.security.BanManager;
import de.poweruser.powerserver.network.UDPSender;
import de.poweruser.powerserver.settings.Settings;

public class ServerList {

    private GameBase game;
    private Map<InetAddress, ServerHost> servers;

    public ServerList(GameBase game) {
        this.game = game;
        this.servers = new HashMap<InetAddress, ServerHost>();
    }

    public boolean incomingHeartBeat(InetSocketAddress serverAddress, MessageData data, boolean manuallyAdded) throws TooManyServersPerHostException {
        if(serverAddress != null) {
            GameServerInterface server = this.getOrCreateServer(serverAddress);
            String serverName = server.getServerName();
            String logMessage = "Received a heartbeat from the server " + serverAddress.toString();
            if(serverName != null) {
                logMessage += (" ( " + serverName + " )");
            }
            Logger.logStatic(LogLevel.VERY_HIGH, logMessage);
            return server.incomingHeartbeat(serverAddress, data, manuallyAdded);
        }
        return false;
    }

    public boolean incomingHeartBeatBroadcast(InetSocketAddress serverAddress, MessageData data) throws TooManyServersPerHostException {
        if(data.containsKey(GeneralDataKeysEnum.HEARTBEATBROADCAST) && data.containsKey(GeneralDataKeysEnum.HOST)) {
            GameServerInterface server = this.getOrCreateServer(serverAddress);
            if(server.isBroadcastedServer()) {
                String serverName = server.getServerName();
                String logMessage = "Received a heartbeat broadcast for the server " + serverAddress.toString();
                if(serverName != null) {
                    logMessage += (" ( " + serverName + " )");
                }
                Logger.logStatic(LogLevel.VERY_HIGH, logMessage);
                return server.incomingHeartBeatBroadcast(serverAddress, data);
            }
        } else {
            Logger.logStatic(LogLevel.HIGH, "Got a heartbeatbroadcast, that is missing the host key");
        }
        return false;
    }

    public boolean incomingQueryAnswer(InetSocketAddress sender, MessageData data) {
        GameServerInterface server = this.getServer(sender);
        if(server != null) { return server.incomingQueryAnswer(sender, data); }
        return false;
    }

    public boolean hasServer(InetSocketAddress server) {
        return this.getServer(server) != null;
    }

    private GameServerInterface getServer(InetSocketAddress server) {
        if(this.servers.containsKey(server.getAddress())) { return this.servers.get(server.getAddress()).getServerOnPort(server.getPort()); }
        return null;
    }

    public boolean isBroadcastedServer(InetSocketAddress server) {
        GameServerInterface gameServer = this.getServer(server);
        if(gameServer != null) { return gameServer.isBroadcastedServer(); }
        return false;
    }

    private GameServerInterface getOrCreateServer(InetSocketAddress server) throws TooManyServersPerHostException {
        if(this.hasServer(server)) {
            return this.getServer(server);
        } else {
            ServerHost host = null;
            InetAddress hostAddress = server.getAddress();
            if(this.servers.containsKey(hostAddress)) {
                host = this.servers.get(hostAddress);
                if(host.getServerCount() >= this.game.getSettings().getMaximumServersPerHost()) { throw new TooManyServersPerHostException(hostAddress); }
            } else {
                host = new ServerHost(hostAddress, this.game);
                this.servers.put(hostAddress, host);
            }
            return host.getOrCreateServer(server.getPort());
        }
    }

    public List<InetSocketAddress> checkForServersToQueryAndOutdatedServers(Settings settings) {
        List<InetSocketAddress> serversToQuery = null;
        Iterator<Entry<InetAddress, ServerHost>> iter = this.servers.entrySet().iterator();
        while(iter.hasNext()) {
            Entry<InetAddress, ServerHost> entry = iter.next();
            ServerHost host = entry.getValue();
            List<InetSocketAddress> list = host.checkForServersToQueryAndOutdatedServers(settings);
            if(list != null) {
                if(serversToQuery != null) {
                    serversToQuery.addAll(list);
                } else {
                    serversToQuery = list;
                }
            } else if(host.getServerCount() == 0) {
                iter.remove();
            }
        }
        return serversToQuery;
    }

    public List<InetSocketAddress> getActiveServers(Settings settings) {
        List<InetSocketAddress> list = null;
        Iterator<Entry<InetAddress, ServerHost>> iter = this.servers.entrySet().iterator();
        while(iter.hasNext()) {
            Entry<InetAddress, ServerHost> entry = iter.next();
            ServerHost host = entry.getValue();
            List<InetSocketAddress> hostList = host.getActiveServers(settings);
            if(hostList != null) {
                if(list == null) {
                    list = hostList;
                } else {
                    list.addAll(hostList);
                }
            }
        }
        return list;
    }

    public void queryServer(InetSocketAddress server, UDPSender udpSender, boolean queryPlayers) {
        GameServerInterface gsi = this.getServer(server);
        if(gsi != null) {
            gsi.markQueryRequestAsSentWithCurrentTime();
            udpSender.queueQuery(server, this.game.createStatusQuery(queryPlayers));
        }
    }

    public void blockHost(InetAddress host, BanManager<InetAddress> banManager, TimeUnit timeUnit, long tempBanDuration) {
        if(banManager.addTempBanByDuration(host, tempBanDuration, timeUnit)) {
            Logger.logStatic(LogLevel.NORMAL, "Temporary ban of " + String.valueOf(tempBanDuration) + " " + timeUnit.toString().toLowerCase() + " for " + host.toString() + ". This host exceeds the 'servers per host' limit");
        }
        if(this.servers.containsKey(host)) {
            this.servers.remove(host);
        }
    }
}
