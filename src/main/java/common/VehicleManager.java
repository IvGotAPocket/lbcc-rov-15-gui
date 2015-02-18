package common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.lwjgl.Sys;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class VehicleManager {

    public static final String COMM_VERSION = "0.1-JSON";
    public static final int MAX_ARDUINO_PACKET = 2048;
    public static final int BROADCAST_PORT = 21025;
    public static final int BROADCAST_DELAY = 3000;
    public static final int UPDATE_DELAY = 500;

    private DatagramSocket socket;
    private ArrayList<Vehicle> vehicles;
    private InetAddress broadcastAddress;

    public VehicleManager() throws IOException {
        vehicles = new ArrayList<>();
        socket = new DatagramSocket(BROADCAST_PORT);
        socket.setBroadcast(true);
        broadcastAddress = InetAddress.getByName("255.255.255.255");
        System.out.println("[VehicleManager] Version: " + COMM_VERSION);
        spinThreads();
    }

    public int count() {
        return vehicles.size();
    }

    public Vehicle waitForFirst() throws InterruptedException {
        while (vehicles.isEmpty()) {
            Thread.sleep(10);
        }
        return vehicles.get(0);
    }

    private void spinThreads() {
        // Start thread that receives data.
        VehicleListenLoop listener = new VehicleListenLoop();
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        // Start thread that sends search requests.
        VehicleScanLoop scanner = new VehicleScanLoop();
        Thread scannerThread = new Thread(scanner);
        scannerThread.start();

        // Start thread that sends updates to vehicles.
        VehicleUpdateLoop updater = new VehicleUpdateLoop();
        Thread updaterThread = new Thread(updater);
        updaterThread.start();

        System.out.println("[VehicleManager] Scanning with port " + BROADCAST_PORT);
    }

    private void processMessage(JSONObject response, DatagramPacket packet) {
        if (response.has("cmd")) {
            switch (response.getString("cmd")) {
                case "ping":
                    // Ignore broadcast echo.
                    break;
                case "pong":
                    processPong(response, packet);
                    break;
                case "chn":
                    processChn(response, packet);
                    break;
                case "is":
                    processIs(response, packet);
                    break;
                default:
                    System.out.print("Unrecognized command: ");
                    System.out.println(response.getString("cmd"));
            }
        }
    }

    /**
     * The ROV will reply to pongs with the computer's control status.
     * Use pongs to maintain contact and understand the ROV.
     */
    private void processPong(JSONObject response, DatagramPacket packet) {
        for (Vehicle v : vehicles) {
            if (v.getAddress().equals(packet.getAddress())) {
                v.setLastComm();
                return;
            }
        }
        Vehicle v = new Vehicle(packet.getAddress(), packet.getPort());
        v.setDetails(response.getInt("chn"), response.getBoolean("ctl"));
        vehicles.add(v);
        System.out.println("Now tracking new vehicle!");

        // Temporary!
        // Ask the ROV to send us info about each channel.
        // The ROV will be "disabled" until we get everything.
        sendListCommand(v);
    }

    private void processChn(JSONObject response, DatagramPacket packet) {
        for (Vehicle v : vehicles) {
            if (v.getAddress().equals(packet.getAddress())) {

                // Verification
                if (response.getInt("num") < 1) throw new IllegalArgumentException();
                if (response.getInt("num") > v.getChannelCount()) throw new IllegalArgumentException();

                // Lookup channel
                Vehicle.Channel channel = null;
                boolean isChannelNew = false;

                for (Vehicle.Channel c : v.channels) {
                    if (c.number == response.getInt("num")) {
                        channel = c;
                    }
                }
                if (channel == null) {
                    channel = new Vehicle.Channel();
                    channel.number = response.getInt("num");
                    isChannelNew = true;
                }

                // Apply settings
                channel.setChannelInfo(
                        response.getString("name"),
                        response.getBoolean("read"),
                        response.getInt("min"),
                        response.getInt("max")
                );

                // Apply last known value included
                channel.setLastKnown(response.getInt("now"));

                if (isChannelNew) {
                    // Set current value to last known, later code change current.
                    // This is so we accept the ROVs default values.
                    channel.current = channel.getLastKnown();
                    // Add the channel to the list.
                    v.channels.add(channel);
                    System.out.println("Added new channel: " + channel.getName());
                }

                return;
            }
        }
        System.out.println("NOT POSSIBRU");
    }


    private void processIs(JSONObject response, DatagramPacket packet) {
        for (Vehicle v : vehicles) {
            if (v.getAddress().equals(packet.getAddress())) {

                JSONArray list = response.getJSONArray("list");

                for (int i = 0; i < list.length(); i++) {
                    JSONObject entry = list.getJSONObject(i);
                    int channel = entry.getInt("c");
                    int value = entry.getInt("v");
                    v.channels.get(channel-1).setLastKnown(value);
                }

                v.setLastComm();
            }
        }
    }

    // ========================================================================
    //  Send Commands
    // ========================================================================

    private void sendUpdate(Vehicle v) {
        String command = VehicleCommand.getSet(v);
        if (command == null) return;
        System.out.println("Sending updated values to ROV.");
        byte[] buffer = command.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        packet.setAddress(v.getAddress());
        packet.setPort(v.getPort());
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendListCommand(Vehicle v) {
        byte[] buffer = VehicleCommand.getList().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        packet.setAddress(v.getAddress());
        packet.setPort(v.getPort());
        try {
            socket.send(packet);
            System.out.println("Sending list command to ROV.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================================================================
    //  Threads
    // ========================================================================

    /**
     * Continuously listens for packets, handling them as they arrive.
     */
    private class VehicleListenLoop implements Runnable {

        private byte[] buffer = new byte[MAX_ARDUINO_PACKET];
        private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        @Override
        public void run() {
            while (true) {
                try {
                    socket.receive(packet);
                    JSONTokener tokener = new JSONTokener(new String(buffer));
                    JSONObject response = new JSONObject(tokener);
                    processMessage(response, packet);
                    packet.setLength(buffer.length);
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    System.out.println("VehicleListenLoop interrupted!");
                } catch (IOException e) {
                    System.out.println("VehicleListenLoop IO exception!");
                } catch (JSONException e) {
                    System.out.println("VehicleListenLoop JSON exception!");
                    System.out.println(new String(packet.getData()));
                }
            }
        }
    }


    /**
     * Continuously sends value update packages to ROVs.
     */
    private class VehicleUpdateLoop implements Runnable {
        @Override
        public void run() {
            while (true) {
                vehicles.forEach(VehicleManager.this::sendUpdate);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Continuously broadcasts requests for ROVs to make themselves known.
     */
    private class VehicleScanLoop implements Runnable {

        // Save the packet: we only have one to send each time.
        private DatagramPacket packet;

        VehicleScanLoop() {
            byte[] buffer = VehicleCommand.getPing(true).getBytes();
            packet = new DatagramPacket(buffer, buffer.length);
            packet.setAddress(broadcastAddress);
            packet.setPort(BROADCAST_PORT);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    socket.send(packet);
                    Thread.sleep(BROADCAST_DELAY);
                } catch (InterruptedException e) {
                    System.out.println("VehicleScanLoop interrupted!");
                } catch (IOException e) {
                    System.out.println("VehicleScanLoop IO exception!");
                }
            }
        }
    }
}
