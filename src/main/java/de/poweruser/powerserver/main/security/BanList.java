package de.poweruser.powerserver.main.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BanList<T> {
    private ConcurrentHashMap<T, Long> banlist;

    public BanList() {
        this.banlist = new ConcurrentHashMap<T, Long>(16, 0.75f, 1);
    }

    public boolean addBan(T item, long duration, TimeUnit unit) {
        if(!this.banlist.containsKey(item)) {
            long milli = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(duration, unit);
            this.banlist.put(item, milli);
            return true;
        }
        return false;
    }

    public boolean isBanned(T item) {
        if(this.banlist.containsKey(item)) {
            long unbanTime = this.banlist.get(item).longValue();
            if(unbanTime > System.currentTimeMillis()) {
                return true;
            } else {
                this.banlist.remove(item);
            }
        }
        return false;
    }
}
