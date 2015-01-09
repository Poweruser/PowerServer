package de.poweruser.powerserver.main.security;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BanList<T> {
    private Map<T, Long> banlist;
    private boolean hasChanged;

    public BanList() {
        this.banlist = new ConcurrentHashMap<T, Long>(16, 0.75f, 1);
        this.hasChanged = false;
    }

    public boolean addTempBanByDuration(T item, long duration, TimeUnit unit) {
        return this.addTempBanByTimeStamp(item, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(duration, unit));
    }

    public boolean addTempBanByTimeStamp(T item, long timeStamp) {
        boolean alreadyContained = this.banlist.containsKey(item);
        this.banlist.put(item, timeStamp);
        this.hasChanged = true;
        return !alreadyContained;
    }

    public boolean addPermBan(T item) {
        return this.addTempBanByTimeStamp(item, Long.MAX_VALUE);
    }

    public boolean isBanned(T item) {
        if(this.banlist.containsKey(item)) {
            long unbanTime = this.banlist.get(item).longValue();
            if(unbanTime > System.currentTimeMillis()) {
                return true;
            } else {
                this.banlist.remove(item);
                this.hasChanged = true;
            }
        }
        return false;
    }

    public String getUnbanDate(T item) {
        if(this.isBanned(item)) { return DateFormat.getInstance().format(new Date(this.banlist.get(item).longValue())); }
        return null;
    }

    public boolean hasChanged() {
        return this.hasChanged;
    }

    public void setUnChanged() {
        this.hasChanged = false;
    }

    public Map<T, Long> getEntries() {
        Map<T, Long> map = new HashMap<T, Long>();
        map.putAll(this.banlist);
        return map;
    }
}
