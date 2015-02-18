package common;

import org.lwjgl.Sys;

import java.net.InetAddress;
import java.util.ArrayList;

public class Vehicle {

    private InetAddress address;
    private int port;

    private long lastComm;
    private long timeout;
    private int channelCount;
    private boolean controlling;

    protected ArrayList<Channel> channels;

    public static class Channel {

        protected int number;

        private String name;
        private boolean readonly;
        private int minimum;
        private int maximum;

        protected int current;

        private int lastKnown;
        private long lastComm;

        protected String getName() {
            return name;
        }

        protected void setCurrent(int value) {
            if (value > maximum) throw new IllegalArgumentException();
            if (value < minimum) throw new IllegalArgumentException();
            current = value;
        }

        protected void setChannelInfo(String name, boolean readonly, int min, int max) {
            if (min > max) throw new IllegalArgumentException("ROV is confused about range on channel " + number);
            this.name = name;
            this.readonly = readonly;
            this.minimum = min;
            this.maximum = max;
            this.lastKnown = -1;
            this.lastComm = -1;
        }

        protected void setLastKnown(int value) {
            // No range checks here, just report what we were told by the ROV.
            lastKnown = value;
            lastComm = System.currentTimeMillis();
        }

        protected int getLastKnown() {
            return lastKnown;
        }

        protected long getLastComm() {
            return lastComm;
        }
    }

    protected Vehicle(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.channels = new ArrayList<>();
    }

    protected void setDetails(int channelCount, boolean controlling) {
        if (channelCount < 0) throw new IllegalArgumentException();
        if (channelCount > 99) throw new IllegalArgumentException();
        this.channelCount = channelCount;
        this.controlling = controlling;
        System.out.println("Vehicle details: " + channelCount + " channels.");
        System.out.println("Vehicle details: Controlling: " + controlling);
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

    protected int getChannelCount() {
        return this.channelCount;
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
