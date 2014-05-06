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

import de.poweruser.powerserver.logger.Logger;

public class TCPManager implements Runnable {

    private ServerSocket serverSocket;
    private Queue<QueryConnection> connections;
    private boolean running;
    private Thread thread;
    private final int CONNECTION_LIMIT = 10;
    private ConnectionGuard guard;

    public TCPManager(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.guard = new ConnectionGuard();
        this.connections = new ConcurrentLinkedQueue<QueryConnection>();
        this.thread = new Thread(this);
        this.thread.setName("PowerServer - TCPServer");
    }

    @Override
    public void run() {
        while(this.running) {
            Socket client = null;
            try {
                client = this.serverSocket.accept();
            } catch(SocketException e) {
                Logger.logStatic(e.toString());
            } catch(IOException e) {
                Logger.logStatic(e.toString());
            }
            if(client != null) {
                if(this.guard.isAnotherConnectionAllowed(client)) {
                    QueryConnection connection = null;
                    try {
                        connection = new QueryConnection(client);
                    } catch(IOException e) {
                        Logger.logStackTraceStatic("Error while creating a QueryConnection: " + e.toString(), e);
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
            }
        }
    }

    private class ConnectionGuard {
        private HashMap<InetAddress, List<QueryConnection>> map;

        public ConnectionGuard() {
            this.map = new HashMap<InetAddress, List<QueryConnection>>();
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
            InetAddress a = client.getInetAddress();
            if(this.map.containsKey(a)) {
                List<QueryConnection> list = this.map.get(client.getInetAddress());
                return (list.size() <= CONNECTION_LIMIT);
            }
            return true;
        }
    }

    public boolean isShutdown() {
        return this.serverSocket.isClosed() && !this.running;
    }
}
