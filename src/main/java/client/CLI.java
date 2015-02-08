/**
 * Created by robmac on 1/21/2015.
 */

package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CLI {

    public static void main(String[] args) throws IOException {

        DatagramSocket socket = new DatagramSocket(21025);
        byte[] buffer = new byte[64];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (true) {

            socket.receive(packet);

            String msg = new String(buffer, 0, packet.getLength());
            System.out.print(packet.getAddress().getHostName());
            System.out.print(": ");
            System.out.println(msg);

            packet.setLength(buffer.length);

        }

    }

}
