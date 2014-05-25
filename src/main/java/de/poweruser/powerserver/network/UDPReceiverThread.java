package de.poweruser.powerserver.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class UDPReceiverThread extends Observable implements Runnable {

    private boolean running;
    private DatagramSocket socket;
    private Thread thread;
    private BanList<InetAddress> banlist;

    public UDPReceiverThread(DatagramSocket socket) throws SocketException {
        this.socket = socket;
        this.banlist = new BanList<InetAddress>();
        this.running = true;
        this.thread = new Thread(this);
        this.thread.setName("PowerServer_UDPReceiverThread");
        this.thread.start();
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
        while(this.running) {
            boolean received = false;
            try {
                this.socket.receive(packet);
                if(!this.banlist.isBanned(packet.getAddress())) {
                    received = true;
                }
            } catch(SocketTimeoutException e) {
                // ignore
            } catch(IOException e) {
                if(this.running) {
                    Logger.logStackTraceStatic(LogLevel.VERY_LOW, "An error occured in the UDPReceiverThread while listening for incoming packets", e);
                }
            }
            if(received && this.running) {
                this.setChanged();
                this.notifyObservers(new UDPMessage(packet));
            }
        }
    }

    public void shutdown() {
        this.running = false;
    }

    protected void banSender(InetAddress sender, long duration, TimeUnit unit) {
        if(this.banlist.addBan(sender, duration, unit)) {
            long minutes = TimeUnit.MINUTES.convert(duration, unit);
            Logger.logStatic(LogLevel.NORMAL, "Temporary Ban of " + minutes + " " + TimeUnit.MINUTES.toString().toLowerCase() + " for " + sender.getHostAddress() + ". Too many incoming UDP packets.");
        }
    }

    private class BanList<T> {
        private HashMap<T, Long> banlist;

        public BanList() {
            this.banlist = new HashMap<T, Long>();
        }

        public boolean addBan(T item, long duration, TimeUnit unit) {
            if(!this.banlist.containsKey(item)) {
                long milli = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(duration, unit);
                this.banlist.put(item, milli);
                return true;
            }
            return false;
        }

        public boolean isBanned(T item) {
            if(this.banlist.containsKey(item)) {
                long unbanTime = this.banlist.get(item).longValue();
                if(unbanTime > System.currentTimeMillis()) {
                    return true;
                } else {
                    this.banlist.remove(item);
                }
            }
            return false;
        }
    }
}
