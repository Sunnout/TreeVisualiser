package plumtree.viewer.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Objects;

public class Host {

    private final int port;
    private final InetAddress address;
    private final byte[] addressBytes;

    public Host(InetAddress address, int port) {
        this(address, address.getAddress(), port);
    }

    private Host(InetAddress address, byte[] addressBytes, int port) {
        if (!(address instanceof Inet4Address)) {
            throw new AssertionError(address + " not and IPv4 address");
        } else {
            this.address = address;
            this.port = port;
            this.addressBytes = addressBytes;

            assert addressBytes.length == 4;

        }
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return this.address.getHostAddress() + ":" + this.port;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof Host)) {
            return false;
        } else {
            Host o = (Host)other;
            return o.port == this.port && o.address.equals(this.address);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.port, this.address});
    }

    public int compareTo(Host other) {
        for(int i = 0; i < 4; ++i) {
            int cmp = Byte.compare(this.addressBytes[i], other.addressBytes[i]);
            if (cmp != 0) {
                return cmp;
            }
        }

        return Integer.compare(this.port, other.port);
    }
}
