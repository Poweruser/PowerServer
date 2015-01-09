package de.poweruser.powerserver.main.security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class SecurityAndBanManager extends SecurityManager implements BanManager<InetAddress> {

    private BanList<InetAddress> banList;
    private File banListFile;
    private String banEntryDelimiter = ";";

    public SecurityAndBanManager() {
        super();
        this.banList = new BanList<InetAddress>();
        this.banListFile = new File("banlist.cfg");
    }

    public synchronized void loadBanListFromFile() {
        if(this.banListFile == null || this.banListFile.isDirectory() || !this.banListFile.exists()) { return; }

        BufferedReader br = null;
        int entriesLoaded = 0;
        try {
            br = new BufferedReader(new FileReader(this.banListFile));
            String line = null;
            while((line = br.readLine()) != null) {
                line = line.trim();
                if(line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                String[] split = line.split("\\" + banEntryDelimiter);
                InetAddress address = null;
                Long timeStamp = null;
                for(int i = 0; i < split.length; i++) {
                    String item = split[i];
                    switch(i) {
                        case 0:
                            try {
                                address = InetAddress.getByName(item);
                            } catch(UnknownHostException e) {}
                            break;
                        case 1:
                            try {
                                timeStamp = Long.parseLong(item);
                            } catch(NumberFormatException e) {}
                            break;
                        default:
                            break;
                    }
                }
                if(address != null && timeStamp != null) {
                    if(this.addTempBanByTimeStamp(address, timeStamp)) {
                        entriesLoaded++;
                    }
                }
            }
            if(entriesLoaded > 0) {
                Logger.logStatic(LogLevel.VERY_LOW, "Ban list loaded. " + String.valueOf(entriesLoaded) + " entries", true);
            }
        } catch(IOException e) {
            Logger.logStatic(LogLevel.VERY_LOW, "Error while loading the ban list from file " + this.banListFile.getName() + ": " + e.toString(), true);
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch(IOException e) {}
            }
        }
    }

    @Override
    public boolean isBanned(InetAddress address) {
        return this.banList.isBanned(address);
    }

    @Override
    public void checkAccept(String host, int port) {
        try {
            InetAddress address = InetAddress.getByName(host);
            if(this.isBanned(address)) { throw new SecurityBanException("The host " + host + " is banned!", address); }
        } catch(UnknownHostException e) {
            throw new SecurityException("The host " + host + " is unknown and cant be resolved!");
        }
    }

    static {
        java.security.Security.setProperty("networkaddress.cache.ttl", "20");
    }

    @Override
    public boolean addTempBanByTimeStamp(InetAddress item, long timeStamp) {
        return this.banList.addTempBanByTimeStamp(item, timeStamp);
    }

    @Override
    public synchronized boolean saveBanListToFile() {
        this.banList.setUnChanged();
        if(!this.banListFile.exists() || !this.banListFile.isFile()) {
            try {
                this.banListFile.createNewFile();
            } catch(IOException e) {
                Logger.logStatic(LogLevel.VERY_LOW, "Failed to create a new file " + this.banListFile.getName() + " to store the ban list:" + e.toString(), true);
                return false;
            }
        }
        if(this.banListFile.exists() && this.banListFile.isFile() && this.banListFile.canWrite()) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(this.banListFile));
                for(Entry<InetAddress, Long> entry: this.banList.getEntries().entrySet()) {
                    String line = entry.getKey().getHostAddress() + banEntryDelimiter + entry.getValue().toString();
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
            } catch(IOException e) {
                Logger.logStatic(LogLevel.VERY_LOW, "Error while saving the ban list to file " + this.banListFile.getName() + ": " + e.toString(), true);
            } finally {
                if(writer != null) {
                    try {
                        writer.close();
                    } catch(IOException e) {}
                }
            }
            return true;
        } else {
            Logger.logStatic(LogLevel.VERY_LOW, "Failed to save the ban list to file " + this.banListFile.getName() + ". Check the write access permissions.", true);
        }
        return false;
    }

    @Override
    public boolean addTempBanByDuration(InetAddress item, long duration, TimeUnit unit) {
        return this.banList.addTempBanByDuration(item, duration, unit);
    }

    @Override
    public boolean addPermBan(InetAddress item) {
        return this.banList.addPermBan(item);
    }

    @Override
    public String getUnbanDate(InetAddress item) {
        return this.banList.getUnbanDate(item);
    }

    @Override
    public boolean hasChanged() {
        return this.banList.hasChanged();
    }
}
