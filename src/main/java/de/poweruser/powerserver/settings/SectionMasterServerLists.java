package de.poweruser.powerserver.settings;

import java.net.MalformedURLException;
import java.net.URL;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;

public class SectionMasterServerLists extends SettingsReader {
    public SectionMasterServerLists(Settings settings) {
        super(settings);
    }

    @Override
    public void readLine(String line) {
        try {
            URL url = new URL(line.trim().toLowerCase());
            this.settings.addMasterServerList(url);
        } catch(MalformedURLException e) {
            Logger.logStatic(LogLevel.VERY_LOW, "The url [ " + line + " ] of the master server list is malformed: " + e.toString());
        }
    }
}
