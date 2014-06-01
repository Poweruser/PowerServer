package de.poweruser.powerserver.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class Settings {

    private File settingsFile;
    private static Settings instance;
    private List<URL> masterServerLists;
    private List<String> masterServers;
    private List<String> supportedGames;
    private int downloadInterval;
    private boolean publicMode;
    private boolean queryServersOnHeartbeat;
    private int maximumServerTimeout;
    private int emergencyQueryInterval;

    private static final int MINIMAL_SERVERTIMEOUT = 20;
    private static final int ALLOWED_HEARTBEATTIMEOUT = 15;

    public Settings(File settingsFile) {
        instance = this;
        this.downloadInterval = 24;
        this.publicMode = true;
        this.queryServersOnHeartbeat = true;
        this.maximumServerTimeout = 60;
        this.calcEmergencyQueryInterval();
        this.masterServerLists = new ArrayList<URL>();
        this.masterServers = new ArrayList<String>();
        this.supportedGames = new ArrayList<String>();
        this.settingsFile = settingsFile;
    }

    public void load() {
        this.masterServerLists.clear();
        this.supportedGames.clear();
        BufferedReader br = null;
        Logger.logStatic(LogLevel.NORMAL, "Loading the settings file ...", true);
        try {
            br = new BufferedReader(new FileReader(this.settingsFile));
            String line = null;
            ConfigSection activeSection = null;
            while((line = br.readLine()) != null) {
                line = line.trim();
                if(line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                if(line.startsWith("[")) {
                    activeSection = ConfigSection.getSection(line);
                    continue;
                }
                if(activeSection != null) {
                    activeSection.readLine(line);
                }
            }
        } catch(IOException e) {
            Logger.logStatic(LogLevel.VERY_LOW, "IO Error while reading settings file: " + this.settingsFile.getName() + ": " + e.getMessage(), true);
        }
        if(br != null) {
            try {
                br.close();
            } catch(IOException e) {}
        }
        Logger.logStatic(LogLevel.LOW, "The server is operating in " + (this.isPublicMode() ? "PUBLIC" : "PRIVATE") + " mode", true);
    }

    enum ConfigSection {
        GENERAL("[General]", new SectionGeneral(Settings.instance)),
        MASTERSERVERLISTS("[MasterServerLists]", new SectionMasterServerLists(Settings.instance)),
        SUPPORTEDGAMES("[SupportedGames]", new SectionSupportedGames(Settings.instance));

        private String sectionIdent;
        private SettingsReaderInterface reader;

        ConfigSection(String sectionIdent, SettingsReaderInterface settingsReader) {
            this.sectionIdent = sectionIdent;
            this.reader = settingsReader;
        }

        public void readLine(String line) {
            this.reader.readLine(line);
        }

        public static ConfigSection getSection(String line) {
            for(ConfigSection c: values()) {
                if(c.sectionIdent.equalsIgnoreCase(line)) { return c; }
            }
            return null;
        }
    }

    protected void addMasterServerList(URL url) {
        if(!this.masterServerLists.contains(url)) {
            this.masterServerLists.add(url);
        }
    }

    protected void addSupportedGame(String gamename) {
        if(!this.supportedGames.contains(gamename)) {
            this.supportedGames.add(gamename);
        }
    }

    protected void setListsDownloadInterval(int hours) {
        if(hours >= 1) {
            this.downloadInterval = hours;
        }
    }

    public long getListsDownloadInterval(TimeUnit outputUnit) {
        return outputUnit.convert((long) this.downloadInterval, TimeUnit.HOURS);
    }

    protected void setPublicMode(boolean active) {
        this.publicMode = active;
    }

    public boolean isPublicMode() {
        return this.publicMode;
    }

    public List<InetAddress> getMasterServerList(boolean forceDownload) {
        if(forceDownload) {
            for(URL list: this.masterServerLists) {
                BufferedReader input = null;
                try {
                    input = new BufferedReader(new InputStreamReader(list.openStream()));
                    String inputLine = null;
                    boolean read = false;
                    while((inputLine = input.readLine()) != null) {
                        inputLine = inputLine.trim().toLowerCase();
                        if(inputLine.startsWith("[\\online]")) {
                            read = false;
                            break;
                        }
                        if(read) {
                            if(!this.masterServers.contains(inputLine)) {
                                this.masterServers.add(inputLine);
                            }
                        }
                        if(inputLine.startsWith("[online]")) {
                            read = true;
                        }
                    }
                } catch(IOException e) {
                    Logger.logStatic(LogLevel.LOW, "Could not read the master server list at " + list.toString() + " - Reason: " + e.toString());
                }
                if(input != null) {
                    try {
                        input.close();
                    } catch(IOException e) {}
                }
            }
        }
        String logMessage = "";
        ArrayList<InetAddress> list = new ArrayList<InetAddress>();
        for(String s: this.masterServers) {
            try {
                InetAddress i = InetAddress.getByName(s);
                list.add(i);
                logMessage += (i.toString() + "\n");
            } catch(UnknownHostException e) {
                Logger.logStatic(LogLevel.LOW, "The master server domain '" + s + "' could not be resolved: " + e.toString());
            }
        }
        if(!logMessage.isEmpty()) {
            Logger.logStatic(LogLevel.LOW, "The loaded master servers are:\n" + logMessage, true);
        }
        return list;
    }

    public List<String> getSupportedGamesList() {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(this.supportedGames);
        return list;
    }

    public boolean getQueryServersOnHeartbeat() {
        return this.queryServersOnHeartbeat;
    }

    protected void setQueryServersOnHeartbeat(boolean active) {
        this.queryServersOnHeartbeat = active;
    }

    protected void setMaximumServerTimeout(int timeout) {
        if(timeout >= MINIMAL_SERVERTIMEOUT) {
            this.maximumServerTimeout = timeout;
            this.calcEmergencyQueryInterval();
        }
    }

    public long getMaximumServerTimeout(TimeUnit unit) {
        return unit.convert(this.maximumServerTimeout, TimeUnit.MINUTES);
    }

    private void calcEmergencyQueryInterval() {
        if(this.maximumServerTimeout >= MINIMAL_SERVERTIMEOUT) {
            this.emergencyQueryInterval = Math.max(1, (this.maximumServerTimeout - ALLOWED_HEARTBEATTIMEOUT) / 5);
        } else {
            this.emergencyQueryInterval = 5;
        }
    }

    public long getEmergencyQueryInterval(TimeUnit unit) {
        return unit.convert(this.emergencyQueryInterval, TimeUnit.MINUTES);
    }

    public long getAllowedHeartbeatTimeout(TimeUnit unit) {
        return unit.convert(ALLOWED_HEARTBEATTIMEOUT, TimeUnit.MINUTES);
    }
}
