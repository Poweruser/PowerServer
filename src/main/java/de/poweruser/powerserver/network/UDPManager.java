package de.poweruser.powerserver.network;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.poweruser.powerserver.main.security.BanManager;
import de.poweruser.powerserver.settings.Settings;

public class UDPManager implements Observer {

    private UDPReceiverThread receiver;
    private UDPSender sender;
    private DatagramSocket socket;
    private Queue<UDPMessage> messageQueue;

    public static final int MAX_MESSAGECOUNT_PER_CYCLE = 50;

    public UDPManager(int port, Settings settings, BanManager<InetAddress> banManager) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(10000);
        this.receiver = new UDPReceiverThread(socket, settings, banManager);
        this.receiver.addObserver(this);
        this.sender = new UDPSender(socket);
        this.messageQueue = new ConcurrentLinkedQueue<UDPMessage>();
    }

    public void shutdown() {
        this.receiver.shutdown();
        this.receiver.deleteObserver(this);
        this.socket.close();
    }

    public boolean isSocketClosed() {
        return this.socket.isClosed();
    }

    @Override
    public void update(Observable observable, Object obj) {
        if(observable.equals(this.receiver)) {
            if(obj instanceof UDPMessage) {
                this.messageQueue.add((UDPMessage) obj);
            }
        }
    }

    public boolean hasMessages() {
        return !this.messageQueue.isEmpty();
    }

    public UDPMessage takeFirstMessage() {
        UDPMessage message = this.messageQueue.poll();
        if(!this.receiver.isBanned(message.getSender().getAddress())) { return message; }
        return null;
    }

    public int getPort() {
        return this.socket.getLocalPort();
    }

    public UDPSender getUDPSender() {
        return this.sender;
    }
}
