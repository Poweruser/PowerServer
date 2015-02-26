package de.poweruser.powerserver.exceptions;

import java.net.InetAddress;

public class TooManyServersPerHostException extends Exception {

    private static final long serialVersionUID = -6878341095943010604L;
    private InetAddress host;

    public TooManyServersPerHostException(InetAddress host) {
        this.host = host;
    }

    public InetAddress getHostAddress() {
        return this.host;
    }
}
