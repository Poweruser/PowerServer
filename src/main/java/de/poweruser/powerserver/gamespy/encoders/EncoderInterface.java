package de.poweruser.powerserver.gamespy.encoders;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public interface EncoderInterface {

    public byte[] encode(List<InetSocketAddress> servers) throws IOException;
}
