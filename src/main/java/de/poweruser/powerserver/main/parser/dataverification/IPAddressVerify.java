package de.poweruser.powerserver.main.parser.dataverification;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddressVerify implements VerificationInterface {

    @Override
    public boolean verify(String data) {
        try {
            // TODO is this already sufficient enough?
            InetAddress address = InetAddress.getByName(data);
            return true;
        } catch(UnknownHostException e) {
            return false;
        }
    }

}
