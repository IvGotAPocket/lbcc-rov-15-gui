package common;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Vehicle {

    private InetAddress address;
    private int port;
    private DatagramSocket socket;
    private long lastComm;

    protected Vehicle(InetAddress address, int port) throws SocketException {
        this.address = address;
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public InetAddress getAddress() {
        return address;
    }

    public void set(int channel, int value) throws IOException {
        JSONObject ping = new JSONObject();
        ping.put("cmd", "set");
        ping.put("channel", channel);
        ping.put("value", value);
        byte[] buffer = ping.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        packet.setAddress(address);
        packet.setPort(port);
        socket.send(packet);
    }
}
