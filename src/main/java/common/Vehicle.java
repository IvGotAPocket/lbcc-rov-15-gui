package common;

import java.net.InetAddress;
import java.util.ArrayList;

public class Vehicle {

    public static final long LOST_TIMEOUT = 1000;

    private InetAddress address;
    private int port;
    private long lastComm;

    protected ArrayList<Channel> channels;

    public static class Channel {
        protected String name;
        protected boolean readonly;
        protected int minimum;
        protected int maximum;
        protected int current;
        protected int lastKnown;
    }

    protected Vehicle(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.channels = new ArrayList<>();

        Channel testChannel = new Channel();
        testChannel.name = "anything";
        testChannel.readonly = false;
        testChannel.minimum = 500;
        testChannel.maximum = 1500;
        testChannel.current = 1000;
        testChannel.lastKnown = 1000;

        channels.add(testChannel);
    }

    protected void setLastComm() {
        this.lastComm = System.currentTimeMillis();
    }

    protected void setLastKnown(int channel, int value) {
        if (channel > 0 && channel <= channels.size()) {
            Channel c = channels.get(channel-1);
            if (value >= c.minimum && value <= c.maximum) {
                c.lastKnown = value;
                System.out.println("ROV reports: channel " + channel + " @ " + value);
            }
        }
    }

    protected InetAddress getAddress() {
        return address;
    }

    protected int getPort() {
        return port;
    }







    public boolean isLost() {
        return (System.currentTimeMillis() - lastComm) > LOST_TIMEOUT;
    }

    // Returns true if the value was accepted and will be sent.
    public boolean set(int channel, int value) {
        if (channel > 0 && channel <= channels.size()) {
            Channel c = channels.get(channel-1);
            if (value >= c.minimum && value <= c.maximum) {
                c.current = value;
                return true;
            }
        }
        return false;
    }

    // Returns last known value of a channel.
    // Returns -1 if channel is not found.
    public int get(int channel) {
        if (channel > 0 && channel <= channels.size()) {
            Channel c = channels.get(channel-1);
            return c.lastKnown;
        }
        return -1;
    }
}
