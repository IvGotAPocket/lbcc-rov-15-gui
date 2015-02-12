/**
 * Created by robmac on 1/21/2015.
 */

package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class CLI {

    public static void main(String[] args) throws IOException {

        DatagramSocket socket = new DatagramSocket(21025);
        byte[] buffer = new byte[64];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        boolean found = false;

        while (true) {

            socket.receive(packet);

            System.out.print(packet.getAddress().getHostName());
            System.out.print(": ");
            System.out.println(Arrays.toString(buffer));


            DatagramPacket something = new DatagramPacket(buffer, packet.getLength());
            something.setAddress(packet.getAddress());
            something.setPort(packet.getPort());
            socket.send(something);

            packet.setLength(buffer.length);

        }

    }

}
