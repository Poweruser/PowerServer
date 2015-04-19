package de.poweruser.powerserver.main;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.exceptions.LocalServerHostException;
import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GameServerBase;
import de.poweruser.powerserver.games.GameServerInterface;
import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.settings.Settings;

public class ServerHost {

    private InetAddress hostAddress;
    private Map<Integer, GameServerInterface> serverList;
    private GameBase gameBase;

    public ServerHost(InetAddress hostAddress, GameBase gameBase) throws LocalServerHostException {
        this.hostAddress = hostAddress;
        if(hostAddress.isLoopbackAddress() || hostAddress.isSiteLocalAddress() || hostAddress.isAnyLocalAddress() || hostAddress.isLinkLocalAddress()) { throw new LocalServerHostException("Local addresses are not permitted for servers."); }
        this.gameBase = gameBase;
        this.serverList = new HashMap<Integer, GameServerInterface>();
    }

    protected GameServerInterface getServerOnPort(int port) {
        if(this.serverList.containsKey(port)) { return this.serverList.get(port); }
        return null;
    }

    public int getServerCount() {
        return this.serverList.size();
    }

    protected GameServerInterface getOrCreateServer(int port) {
        if(this.serverList.containsKey(port)) {
            return this.serverList.get(port);
        } else {
            GameServerInterface server = this.gameBase.createNewServer(new InetSocketAddress(this.hostAddress, port));
            this.serverList.put(port, server);
            return server;
        }
    }

    protected List<InetSocketAddress> checkForServersToQueryAndOutdatedServers(Settings settings) {
        List<InetSocketAddress> serversToQuery = null;
        Iterator<Entry<Integer, GameServerInterface>> iter = this.serverList.entrySet().iterator();
        while(iter.hasNext()) {
            GameServerInterface gsi = iter.next().getValue();
            InetSocketAddress socketAddress = gsi.getSocketAddress();
            if(!gsi.checkLastHeartbeat(settings.getAllowedHeartbeatTimeout(TimeUnit.MINUTES), TimeUnit.MINUTES)) {
                if(!gsi.checkLastMessage(settings.getMaximumServerTimeout(TimeUnit.MINUTES), TimeUnit.MINUTES)) {
                    iter.remove();
                    Logger.logStatic(LogLevel.NORMAL, "Removed server " + socketAddress.toString() + " (" + gsi.getServerName() + ") of game " + ((GameServerBase) gsi).getDisplayName() + ". Timeout reached.");
                } else if(!gsi.checkLastQueryRequest(settings.getEmergencyQueryInterval(TimeUnit.MINUTES), TimeUnit.MINUTES)) {
                    if(serversToQuery == null) {
                        serversToQuery = new ArrayList<InetSocketAddress>();
                    }
                    serversToQuery.add(socketAddress);
                    String logMessage = "The server " + socketAddress.toString();
                    String serverName = gsi.getServerName();
                    if(serverName != null) {
                        logMessage += " (" + serverName + ")";
                    }
                    logMessage += " does not send heartbeats anymore, or they dont reach this server. Sending back a query instead.";
                    Logger.logStatic(LogLevel.VERY_HIGH, logMessage);
                }
            }
        }
        return serversToQuery;
    }

    protected List<InetSocketAddress> getActiveServers(Settings settings) {
        List<InetSocketAddress> activeServers = null;
        Iterator<Entry<Integer, GameServerInterface>> iter = this.serverList.entrySet().iterator();
        while(iter.hasNext()) {
            GameServerInterface gsi = iter.next().getValue();
            if(gsi.checkLastMessage(settings.getMaximumServerTimeout(TimeUnit.MINUTES), TimeUnit.MINUTES)) {
                if(gsi.hasAnsweredToQuery()) {
                    if(activeServers == null) {
                        activeServers = new ArrayList<InetSocketAddress>();
                    }
                    activeServers.add(gsi.getSocketAddress());
                }
            }
        }
        return activeServers;
    }
}
