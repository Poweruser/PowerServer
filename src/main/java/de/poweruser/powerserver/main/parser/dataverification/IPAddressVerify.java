package de.poweruser.powerserver.main.parser.dataverification;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddressVerify implements VerificationInterface {

    private InetAddress verifiedAddress;

    @Override
    public boolean verify(String data) {
        try {
            // TODO is this already sufficient enough?
            this.verifiedAddress = InetAddress.getByName(data);
            return true;
        } catch(UnknownHostException e) {
            return false;
        }
    }

    public InetAddress getVerifiedAddress() {
        return this.verifiedAddress;
    }

}
