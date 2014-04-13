package de.poweruser.powerserver.settings;

public abstract class SettingsReader implements SettingsReaderInterface {

    protected Settings settings;

    public SettingsReader(Settings settings) {
        this.settings = settings;
    }
}
