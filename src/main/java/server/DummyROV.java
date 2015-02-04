package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by robmac on 1/21/2015.
 */
public class DummyROV {

    public static void main(String[] args) throws IOException {

        ServerSocket welcomeSocket = new ServerSocket(6789);

        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            char[] packet = new char[256];
            inFromClient.read(packet);
            System.out.println("Client send:" + packet);

            // Always send okay.
            outToClient.writeBytes("OK\n");
        }
    }

}
