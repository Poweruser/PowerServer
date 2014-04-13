package de.poweruser.powerserver.settings;

public class SectionSupportedGames extends SettingsReader {

    public SectionSupportedGames(Settings settings) {
        super(settings);
    }

    @Override
    public void readLine(String line) {
        this.settings.addSupportedGame(line.trim());
    }
}
