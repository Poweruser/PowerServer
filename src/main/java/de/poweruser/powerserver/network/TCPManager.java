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
import de.poweruser.powerserver.main.security.SecurityBanException;
import de.poweruser.powerserver.network.QueryConnection.State;

public class TCPManager implements Runnable {

    private ServerSocket serverSocket;
    private Queue<QueryConnection> connections;
    private boolean running;
    private Thread thread;
    private final int CONNECTION_LIMIT = 5;
    private ConnectionGuard guard;

    public TCPManager(int port) throws IOException {
        this.running = true;
        this.serverSocket = new ServerSocket(port);
        this.guard = new ConnectionGuard();
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

            } catch(SecurityException e) {

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
        private HashMap<InetAddress, Long> banlist;

        private final long BAN_DURATION = 15L;
        private final TimeUnit BAN_UNIT = TimeUnit.MINUTES;

        public ConnectionGuard() {
            this.map = new HashMap<InetAddress, List<QueryConnection>>();
            this.banlist = new HashMap<InetAddress, Long>();
        }

        public void untrackConnection(QueryConnection c) {
            InetAddress a = c.getClientAddress();
            if(this.map.containsKey(a)) {
                List<QueryConnection> list = this.map.get(a);
                list.remove(c);
            }
        }

        public void trackConnection(QueryConnection c) {
            InetAddress a = c.getClientAddress();
            List<QueryConnection> list;
            if(this.map.containsKey(a)) {
                list = this.map.get(a);
            } else {
                list = new ArrayList<QueryConnection>();
            }
            list.add(c);
        }

        public boolean isAnotherConnectionAllowed(Socket client) {
            InetAddress address = client.getInetAddress();
            if(this.isBanned(address)) {
                if(this.canBanBeLifted(address, 15L, TimeUnit.MINUTES)) {
                    this.banlist.remove(address);
                } else {
                    return false;
                }
            }

            if(this.map.containsKey(address)) {
                List<QueryConnection> list = this.map.get(address);
                boolean allowed = (list.size() < CONNECTION_LIMIT);
                if(!allowed) {
                    this.ban(address);
                }
                return allowed;
            }
            return true;
        }

        private void ban(InetAddress address) {
            this.banlist.put(address, System.currentTimeMillis());
            if(this.map.containsKey(address)) {
                List<QueryConnection> list = this.map.get(address);
                for(QueryConnection q: list) {
                    q.forceClose();
                }
            }
            Logger.logStatic(LogLevel.NORMAL, "Temporary ban of " + BAN_DURATION + " " + BAN_UNIT.toString().toLowerCase() + " for " + address + ". Too many open connections");
        }

        private boolean canBanBeLifted(InetAddress address, long duration, TimeUnit unit) {
            long time = TimeUnit.MILLISECONDS.convert(duration, unit);
            if(this.isBanned(address)) { return (System.currentTimeMillis() - time) > this.banlist.get(address); }
            return true;
        }

        public boolean isBanned(InetAddress address) {
            return this.banlist.containsKey(address);
        }
    }

    public boolean isShutdown() {
        return this.serverSocket.isClosed() && !this.running;
    }
}
