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

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByAddress(new byte[] {10,0,100,19});
        byte[] sendData;
        byte[] receiveData = new byte[1024];
        String sentence = inFromUser.readLine();
        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 8888);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence);
        clientSocket.close();

    }



    public static class ROV {

        public static enum CommandType {
            GET,
            SET
        }

        private InetAddress ip;
        private int port;
        private byte[] secret;

        public ROV (InetAddress ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public boolean cmd(CommandType type, byte[] params) {

        }
    }

    public static boolean emit(InetAddress ip, int port, String message) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            byte[] data = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, ip, port);
            clientSocket.send(sendPacket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


}
