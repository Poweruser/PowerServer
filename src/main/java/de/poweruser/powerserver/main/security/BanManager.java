package de.poweruser.powerserver.main.security;

import java.util.concurrent.TimeUnit;

public interface BanManager<T> {

    public boolean isBanned(T item);

    public boolean addTempBanByTimeStamp(T item, long timeStamp);

    public boolean addTempBanByDuration(T item, long duration, TimeUnit unit);

    public boolean addPermBan(T item);

    public boolean saveBanListToFile();

    public String getUnbanDate(T item);

    public boolean hasChanged();
}
