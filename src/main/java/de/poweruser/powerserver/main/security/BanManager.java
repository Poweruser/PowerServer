package de.poweruser.powerserver.main.security;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;


public interface BanManager {

    public boolean isBanned(InetAddress address);

    public boolean addBan(InetAddress address, long duration, TimeUnit unit);

    public BanList<InetAddress> getBanList();
}
