package de.poweruser.powerserver.network;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

public class UDPManager implements Observer {

    private UDPReceiverThread receiver;
    private UDPSender sender;
    private DatagramSocket socket;
    private Queue<UDPMessage> messageQueue;

    public UDPManager(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(10000);
        this.receiver = new UDPReceiverThread(socket);
        this.sender = new UDPSender(socket);
    }

    public void shutdown() {
        this.receiver.shutdown();
        this.receiver.deleteObserver(this);
        this.socket.close();
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
}
