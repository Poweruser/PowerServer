package de.poweruser.powerserver.settings;

import de.poweruser.powerserver.logger.LogLevel;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.parser.dataverification.BooleanVerify;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public class SectionGeneral extends SettingsReader {

    public SectionGeneral(Settings settings) {
        super(settings);
    }

    @Override
    public void readLine(String line) {
        String[] split = line.split("=");
        if(split.length == 2) {
            String key = split[0].trim();
            String value = split[1].trim();
            IntVerify intVerifier;
            BooleanVerify boolVerifier;
            if(key.equalsIgnoreCase("masterserverlistsdownloadinterval")) {
                intVerifier = new IntVerify(0, Integer.MAX_VALUE);
                if(intVerifier.verify(value)) {
                    settings.setListsDownloadInterval(intVerifier.getVerifiedValue());
                }
            } else if(key.equalsIgnoreCase("publicmode")) {
                boolVerifier = new BooleanVerify();
                settings.setPublicMode(boolVerifier.verify(value));
            } else if(key.equalsIgnoreCase("loglevel")) {
                intVerifier = new IntVerify(0, LogLevel.getMaxLevel().getValue());
                if(intVerifier.verify(value)) {
                    Logger.setLogLevel(intVerifier.getVerifiedValue());
                }
            } else if(key.equalsIgnoreCase("queryServersOnHeartbeat")) {
                boolVerifier = new BooleanVerify();
                settings.setQueryServersOnHeartbeat(boolVerifier.verify(value));
            } else if(key.equalsIgnoreCase("maximumServerTimeout")) {
                intVerifier = new IntVerify(0, Integer.MAX_VALUE);
                if(intVerifier.verify(value)) {
                    settings.setMaximumServerTimeout(intVerifier.getVerifiedValue());
                }
            } else if(key.equalsIgnoreCase("maximumServersPerHost")) {
                intVerifier = new IntVerify(0, Integer.MAX_VALUE);
                if(intVerifier.verify(value)) {
                    settings.setMaximumServersPerHost(intVerifier.getVerifiedValue());
                }
            } else if(key.equalsIgnoreCase("maximumSendViolations")) {
                intVerifier = new IntVerify(0, Integer.MAX_VALUE);
                if(intVerifier.verify(value)) {
                    settings.setMaximumSendViolations(intVerifier.getVerifiedValue());
                }
            } else if(key.equalsIgnoreCase("allowedMinimumSendInterval")) {
                intVerifier = new IntVerify(1, Integer.MAX_VALUE);
                if(intVerifier.verify(value)) {
                    settings.setAllowedMinimumSendInterval(intVerifier.getVerifiedValue());
                }
            } else if(key.equalsIgnoreCase("tempBanDuration")) {
                intVerifier = new IntVerify(0, Integer.MAX_VALUE);
                if(intVerifier.verify(value)) {
                    settings.setTempBanDuration(intVerifier.getVerifiedValue());
                }
            } else if(key.equalsIgnoreCase("connectionLimitPerClient")) {
                intVerifier = new IntVerify(1, Integer.MAX_VALUE);
                if(intVerifier.verify(value)) {
                    settings.setConnectionLimitPerClient(intVerifier.getVerifiedValue());
                }
            }
        }
    }
}
