package de.poweruser.powerserver.network;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class UDPManager implements Observer {

    private UDPReceiverThread receiver;
    private UDPSender sender;
    private DatagramSocket socket;
    private Queue<UDPMessage> messageQueue;

    public static final int MAX_MESSAGECOUNT_PER_CYCLE = 50;

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

    public void checkUDPOverload() {
        int messageCount = this.messageQueue.size();
        if(messageCount > (MAX_MESSAGECOUNT_PER_CYCLE * 10)) {
            HashMap<InetAddress, Integer> map = new HashMap<InetAddress, Integer>();
            Iterator<UDPMessage> iter = this.messageQueue.iterator();
            while(iter.hasNext()) {
                UDPMessage m = iter.next();
                InetAddress sender = m.getSender().getAddress();
                int i = 0;
                if(map.containsKey(sender)) {
                    i = map.get(sender).intValue();
                }
                i++;
                map.put(sender, i);
            }
            for(Entry<InetAddress, Integer> entry: map.entrySet()) {
                int count = entry.getValue().intValue();
                if(count * 10 > messageCount) {
                    this.receiver.banSender(entry.getKey(), 15L, TimeUnit.MINUTES);
                }
            }
            map.clear();
        }
    }
}
