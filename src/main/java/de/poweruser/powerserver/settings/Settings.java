package de.poweruser.powerserver.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Settings {

    private File settingsFile;
    private static Settings instance;
    private List<String> masterServers;
    private List<String> supportedGames;
    private int udpPort;

    public Settings(File settingsFile) {
        this.instance = this;
        this.masterServers = new ArrayList<String>();
        this.supportedGames = new ArrayList<String>();
        this.settingsFile = settingsFile;
    }

    public void load() {
        this.masterServers.clear();
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

    protected void addMasterServer(String domain) {
        if(!this.masterServers.contains(domain)) {
            this.masterServers.add(domain);
        }
    }

    protected void addSupportedGame(String gamename) {
        if(!this.supportedGames.contains(gamename)) {
            this.supportedGames.add(gamename);
        }
    }

    public List<InetAddress> getMasterServerList() {
        ArrayList<InetAddress> list = new ArrayList<InetAddress>();
        for(String s: this.masterServers) {
            try {
                InetAddress i = InetAddress.getByName(s);
                list.add(i);
            } catch(UnknownHostException e) {}
        }
        return list;
    }

    public List<String> getSupportedGamesList() {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(this.supportedGames);
        return list;
    }

    public int getUDPPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    protected void setUDPPort(int port) {

    }
}
