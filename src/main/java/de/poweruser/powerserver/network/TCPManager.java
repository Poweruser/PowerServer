package de.poweruser.powerserver.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.security.BanList;
import de.poweruser.powerserver.main.security.BanManager;
import de.poweruser.powerserver.main.security.SecurityBanException;
import de.poweruser.powerserver.network.QueryConnection.State;
import de.poweruser.powerserver.settings.Settings;

public class TCPManager implements Runnable {

    private ServerSocket serverSocket;
    private Queue<QueryConnection> connections;
    private boolean running;
    private Thread thread;
    private ConnectionGuard guard;
    private Settings settings;

    public TCPManager(int port, Settings settings, BanManager banManager) throws IOException {
        this.running = true;
        this.serverSocket = new ServerSocket(port);
        this.settings = settings;
        this.guard = new ConnectionGuard(banManager);
        this.connections = new ConcurrentLinkedQueue<QueryConnection>();
        this.thread = new Thread(this);
        this.thread.setName("PowerServer - TCPManager");
        this.thread.start();
    }

    @Override
    public void run() {
        while(this.running) {
            Socket client = null;
            try {
                client = this.serverSocket.accept();
            } catch(SecurityBanException e) {
                InetAddress host = e.getBannedHost();
                this.guard.checkBan(host);
            } catch(SecurityException e) {
                Logger.logStackTraceStatic(LogLevel.VERY_LOW, "SecurityException while accepting an incoming TCP connection.", e);
            } catch(SocketException e) {
                if(this.running) {
                    Logger.logStatic(LogLevel.VERY_LOW, "The TCPManager failed to accept an incoming query connection: " + e.toString());
                }
            } catch(IOException e) {
                if(this.running) {
                    Logger.logStatic(LogLevel.VERY_LOW, "The TCPManager failed to accept an incoming query connection: " + e.toString());
                }
            }
            if(client != null) {
                if(this.guard.isAnotherConnectionAllowed(client)) {
                    QueryConnection connection = null;
                    try {
                        connection = new QueryConnection(client);
                    } catch(IOException e) {
                        Logger.logStackTraceStatic(LogLevel.LOW, "Error while creating a QueryConnection: " + e.toString(), e);
                    }
                    if(connection != null) {
                        this.connections.add(connection);
                        this.guard.trackConnection(connection);
                    }
                } else {
                    try {
                        client.close();
                    } catch(IOException e) {}
                }
            }
        }
    }

    public boolean isSocketClosed() {
        return this.serverSocket.isClosed();
    }

    public void shutdown() {
        this.running = false;
        Iterator<QueryConnection> iter = this.connections.iterator();
        while(iter.hasNext()) {
            QueryConnection c = iter.next();
            c.close();
            iter.remove();
            this.guard.untrackConnection(c);
        }
        try {
            this.serverSocket.close();
        } catch(IOException e) {}
    }

    public void processConnections() {
        Iterator<QueryConnection> iter = this.connections.iterator();
        while(iter.hasNext()) {
            QueryConnection c = iter.next();
            if(c.check()) {
                this.guard.untrackConnection(c);
                iter.remove();
                State failed = c.getFailedState();
                if(failed != null) {
                    String logMessage = "QUERY Failed. State: " + failed.toString();
                    String failMessage = c.getFailMessage();
                    if(failMessage != null) {
                        logMessage += " Reason: " + failMessage;
                    }
                    Logger.logStatic(LogLevel.LOW, logMessage);
                }
            }
        }
    }

    private class ConnectionGuard {
        private HashMap<InetAddress, List<QueryConnection>> map;
        private BanList<InetAddress> banlist;

        public ConnectionGuard(BanManager banManager) {
            this.map = new HashMap<InetAddress, List<QueryConnection>>();
            if(banManager != null) {
                this.banlist = banManager.getBanList();
            } else {
                this.banlist = new BanList<InetAddress>(false);
            }
        }

        public void checkBan(InetAddress host) {
            this.banlist.isBanned(host);
        }

        public void untrackConnection(QueryConnection c) {
            InetAddress a = c.getClientAddress();
            if(this.map.containsKey(a)) {
                List<QueryConnection> list = this.map.get(a);
                list.remove(c);
                if(list.isEmpty()) {
                    this.map.remove(a);
                }
            }
        }

        public void trackConnection(QueryConnection c) {
            InetAddress a = c.getClientAddress();
            List<QueryConnection> list;
            if(this.map.containsKey(a)) {
                list = this.map.get(a);
            } else {
                list = new ArrayList<QueryConnection>();
                this.map.put(a, list);
            }
            list.add(c);
        }

        public boolean isAnotherConnectionAllowed(Socket client) {
            InetAddress address = client.getInetAddress();
            if(this.banlist.isBanned(address)) { return false; }

            if(this.map.containsKey(address)) {
                List<QueryConnection> list = this.map.get(address);
                boolean allowed = (list.size() < settings.getConnectionLimitPerClient());
                if(!allowed) {
                    this.ban(address);
                }
                return allowed;
            }
            return true;
        }

        private void ban(InetAddress address) {
            TimeUnit unit = TimeUnit.MINUTES;
            long duration = settings.getTempBanDuration(unit);
            if(this.banlist.addBan(address, duration, unit)) {
                Logger.logStatic(LogLevel.NORMAL, "Temporary ban of " + duration + " " + unit.toString().toLowerCase() + " for " + address.toString() + ". Too many open connections");
            }
            if(this.map.containsKey(address)) {
                List<QueryConnection> list = this.map.get(address);
                for(QueryConnection q: list) {
                    q.forceClose();
                }
            }
        }
    }

    public boolean isShutdown() {
        return this.serverSocket.isClosed() && !this.running;
    }
}
