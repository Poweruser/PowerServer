package de.poweruser.powerserver.settings;

public class SectionMasterServerLists extends SettingsReader {
    public SectionMasterServerLists(Settings settings) {
        super(settings);
    }

    @Override
    public void readLine(String line) {
        this.settings.addMasterServer(line.trim());
    }
}
