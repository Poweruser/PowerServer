package de.poweruser.powerserver.main.security;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.network.BanList;

public interface BanManager {

    public boolean isBanned(InetAddress address);

    public boolean addBan(InetAddress address, long duration, TimeUnit unit);

    public BanList<InetAddress> getBanList();
}
