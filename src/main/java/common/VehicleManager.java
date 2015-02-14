package common;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class VehicleManager {

    public static final String COMMS_VERSION = "0.1-JSON";
    public static final int MAX_ARDUINO_PACKET = 64;
    public static final int BROADCAST_PORT = 21025;
    public static final int BROADCAST_DELAY = 3000;

    private DatagramSocket socket;
    private ArrayList<Vehicle> vehicles;

    public VehicleManager() throws IOException {

        vehicles = new ArrayList<>();
        socket = new DatagramSocket(BROADCAST_PORT);
        socket.setBroadcast(true);

        VehicleListenLoop listener = new VehicleListenLoop();
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        VehicleScanLoop scanner = new VehicleScanLoop();
        Thread scannerThread = new Thread(scanner);
        scannerThread.start();

        System.out.println("[VehicleManager] Version: " + COMMS_VERSION);
        System.out.println("[VehicleManager] Scanning with port " + BROADCAST_PORT);
    }

    /**
     * Continuously broadcasts requests for ROVs to make themselves known.
     */
    private class VehicleScanLoop implements Runnable {

        private DatagramPacket packet;

        VehicleScanLoop() throws UnknownHostException {
            JSONObject ping = new JSONObject();
            ping.put("ping", "rov");
            byte[] buffer = ping.toString().getBytes();
            packet = new DatagramPacket(buffer, buffer.length);
            packet.setAddress(InetAddress.getByName("255.255.255.255"));
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
                    processMessage(response, packet.getAddress(), packet.getPort());
                    packet.setLength(buffer.length);
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    System.out.println("VehicleListenLoop interrupted!");
                } catch (IOException e) {
                    System.out.println("VehicleListenLoop IO exception!");
                } catch (JSONException e) {
                    System.out.println("VehicleListenLoop JSON exception!!");
                }
            }
        }
    }

    private void processMessage(JSONObject response, InetAddress ip, int port) {
        if (response.has("pong")) {
            for (Vehicle v : vehicles) {
                if (v.getAddress().equals(ip)) {
                    return;
                }
            }
            try {
                Vehicle v = new Vehicle(ip, port);
                vehicles.add(v);
                System.out.println("I found a new ROV: " + response.getString("pong"));
            } catch (SocketException socket) {
                System.out.println("I found a new ROV, but I couldn't connect to it.");
            }
        }
    }

    public int count() {
        return vehicles.size();
    }

    public Vehicle getFirstVehicle() {
        if (!vehicles.isEmpty()) {
            return vehicles.get(0);
        } else {
            return null;
        }
    }

}
