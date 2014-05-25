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

    public Settings(File settingsFile) {
        instance = this;
        this.downloadInterval = 24;
        this.publicMode = true;
        this.masterServerLists = new ArrayList<URL>();
        this.masterServers = new ArrayList<String>();
        this.supportedGames = new ArrayList<String>();
        this.settingsFile = settingsFile;
    }

    public void load() {
        this.masterServerLists.clear();
        this.supportedGames.clear();
        BufferedReader br = null;
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
            System.out.println("IO Error while reading settings file: " + this.settingsFile.getName() + ": " + e.getMessage());
        }
        if(br != null) {
            try {
                br.close();
            } catch(IOException e) {}
        }
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
        ArrayList<InetAddress> list = new ArrayList<InetAddress>();
        for(String s: this.masterServers) {
            try {
                InetAddress i = InetAddress.getByName(s);
                list.add(i);
            } catch(UnknownHostException e) {
                Logger.logStatic(LogLevel.LOW, "The master server domain '" + s + "' could not be resolved: " + e.toString());
            }
        }
        return list;
    }

    public List<String> getSupportedGamesList() {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(this.supportedGames);
        return list;
    }
}
