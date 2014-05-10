package de.poweruser.powerserver.network;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPManager implements Observer {

    private UDPReceiverThread receiver;
    private UDPSender sender;
    private DatagramSocket socket;
    private Queue<UDPMessage> messageQueue;

    public UDPManager(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(10000);
        this.receiver = new UDPReceiverThread(socket);
        this.receiver.addObserver(this);
        this.sender = new UDPSender(socket);
        this.messageQueue = new ConcurrentLinkedQueue<UDPMessage>();
    }

    public void shutdown() {
        this.receiver.shutdown();
        this.receiver.deleteObserver(this);
        this.socket.close();
    }

    public boolean isShutdown() {
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
        return this.messageQueue.poll();
    }

    public int getPort() {
        return this.socket.getLocalPort();
    }

    public UDPSender getUDPSender() {
        return this.sender;
    }
}
