package de.poweruser.powerserver.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConsoleReader implements Runnable {

    private PowerServer server;
    private boolean running;
    private Thread thread;
    private BufferedReader reader;
    private Object waitObject;

    public ConsoleReader(PowerServer server) {
        this.server = server;
        this.running = true;
        this.waitObject = new Object();
        this.thread = new Thread(this);
        this.thread.setName("PowerServer - ConsoleReader");
        this.thread.start();
    }

    @Override
    public void run() {
        InputStream input = System.in;
        InputStreamReader isr = new InputStreamReader(System.in);
        this.reader = new BufferedReader(isr);

        while(this.running) {
            String line = null;
            try {
                if(input.available() > 0) {
                    line = this.reader.readLine();
                    if(line != null) {
                        this.server.queueCommand(line);
                    }
                } else {
                    synchronized(this.waitObject) {
                        try {
                            this.waitObject.wait(1000L);
                        } catch(InterruptedException e) {}
                    }
                }
            } catch(IOException e) {}
        }
        try {
            this.reader.close();
        } catch(IOException e) {}
    }

    public void shutdown() {
        this.running = false;
        synchronized(this.waitObject) {
            this.waitObject.notifyAll();
        }
        try {
            this.reader.close();
        } catch(IOException e) {}
    }
}
