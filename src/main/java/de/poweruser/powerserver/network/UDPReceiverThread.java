package de.poweruser.powerserver.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.security.BanList;
import de.poweruser.powerserver.main.security.BanManager;
import de.poweruser.powerserver.main.security.SecurityBanException;
import de.poweruser.powerserver.settings.Settings;

public class UDPReceiverThread extends Observable implements Runnable {

    private boolean running;
    private DatagramSocket socket;
    private Thread thread;
    private BanList<InetAddress> banlist;
    private PacketFilter packetFilter;
    private Settings settings;

    public UDPReceiverThread(DatagramSocket socket, Settings settings, BanManager banManager) throws SocketException {
        this.socket = socket;
        if(banManager != null) {
            this.banlist = banManager.getBanList();
        } else {
            this.banlist = new BanList<InetAddress>();
        }
        this.settings = settings;
        this.packetFilter = new PacketFilter(settings);
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
                    InetAddress sender = packet.getAddress();
                    if(this.packetFilter.newIncoming(sender, System.currentTimeMillis())) {
                        received = true;
                    } else {
                        this.banSender(sender, this.settings.getTempBanDuration(TimeUnit.MINUTES), TimeUnit.MINUTES);
                    }
                }
            } catch(SecurityBanException e) {
                InetAddress host = e.getBannedHost();
                this.banlist.isBanned(host);
            } catch(SecurityException e) {
                Logger.logStackTraceStatic(LogLevel.VERY_LOW, "SecurityException while receiving a UDP packet.", e);
            } catch(SocketTimeoutException e) {
                this.packetFilter.cleanup();
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
            Logger.logStatic(LogLevel.NORMAL, "Temporary Ban of " + minutes + " " + TimeUnit.MINUTES.toString().toLowerCase() + " for " + sender.getHostAddress() + ". Too much data is incoming too fast from this host.");
        }
    }

    public boolean isBanned(InetAddress address) {
        return this.banlist.isBanned(address);
    }

}
