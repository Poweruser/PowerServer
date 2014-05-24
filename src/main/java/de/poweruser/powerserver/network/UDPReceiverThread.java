package de.poweruser.powerserver.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class UDPReceiverThread extends Observable implements Runnable {

    private boolean running;
    private DatagramSocket socket;
    private Thread thread;

    public UDPReceiverThread(DatagramSocket socket) throws SocketException {
        this.socket = socket;
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
                received = true;
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
}
