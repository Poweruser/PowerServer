package de.poweruser.powerserver.gamespy.encoders;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OFPMonitorEncoder implements EncoderInterface {

    /*
     * Data-Format
     * 
     * 8Bytes Header contains:
     * Integer - Length (number of bytes) of IPV4 addresses. Each address takes
     * 6Bytes (4 for IP, 2 for Port)
     * Integer - Length (number of bytes) of IPV6 addresses. Each address takes
     * 18Bytes (16 for IP, 2 for Port)
     * 
     * IPV4 addresses
     * IPV6 addresses
     */

    @Override
    public byte[] encode(List<InetSocketAddress> servers) throws IOException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteArray);
        List<InetSocketAddress> ipv4 = new ArrayList<InetSocketAddress>();
        List<InetSocketAddress> ipv6 = new ArrayList<InetSocketAddress>();
        Iterator<InetSocketAddress> iter = servers.iterator();
        while(iter.hasNext()) {
            InetSocketAddress i = iter.next();
            InetAddress address = i.getAddress();
            if(address instanceof Inet4Address) {
                ipv4.add(i);
            } else if(address instanceof Inet6Address) {
                ipv6.add(i);
            }
            iter.remove();
        }
        int ipv4Len = ipv4.size() * 6;
        int ipv6Len = ipv6.size() * 18;
        stream.writeInt(ipv4Len);
        stream.writeInt(ipv6Len);
        for(InetSocketAddress i: ipv4) {
            this.writeAddress(stream, i);
        }
        for(InetSocketAddress i: ipv6) {
            this.writeAddress(stream, i);
        }
        stream.flush();
        byte[] out = byteArray.toByteArray();
        stream.close();
        return out;
    }

    private void writeAddress(DataOutputStream stream, InetSocketAddress address) throws IOException {
        stream.write(address.getAddress().getAddress());
        int port = address.getPort();
        stream.writeByte((port >> 8) & 0xFF);
        stream.writeByte(port & 0xFF);
    }
}
