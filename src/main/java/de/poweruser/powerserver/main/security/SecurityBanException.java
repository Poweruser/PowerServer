package de.poweruser.powerserver.main.security;

import java.net.InetAddress;

public class SecurityBanException extends SecurityException {

    private static final long serialVersionUID = 2169133405309368766L;
    private final InetAddress bannedHost;

    public SecurityBanException(String message, InetAddress bannedHost) {
        super(message);
        this.bannedHost = bannedHost;
    }

    public InetAddress getBannedHost() {
        return this.bannedHost;
    }
}
