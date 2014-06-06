package de.poweruser.powerserver.main.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class SecurityAndBanManager extends SecurityManager implements BanManager {

    private BanList<InetAddress> banList;

    public SecurityAndBanManager() {
        super();
        this.banList = new BanList<InetAddress>(true);
    }

    @Override
    public boolean isBanned(InetAddress address) {
        return this.banList.isBanned(address);
    }

    @Override
    public boolean addBan(InetAddress address, long duration, TimeUnit unit) {
        return this.banList.addBan(address, duration, unit);
    }

    @Override
    public BanList<InetAddress> getBanList() {
        return this.banList;
    }

    @Override
    public void checkAccept(String host, int port) {
        try {
            InetAddress address = InetAddress.getByName(host);
            if(this.isBanned(address)) { throw new SecurityBanException("The host " + host + " is banned!", address); }
        } catch(UnknownHostException e) {
            throw new SecurityException("The host " + host + " is unknown and cant be resolved!");
        }
    }

    static {
        java.security.Security.setProperty("networkaddress.cache.ttl", "20");
    }
}
