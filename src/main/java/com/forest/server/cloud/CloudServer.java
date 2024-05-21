package com.forest.server.cloud;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class CloudServer {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            // Create a ZeroMQ reply socket
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5556");

            System.out.println("Listening...");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String message = new String(reply, ZMQ.CHARSET);
                System.out.println("Received: [" + message + "]");

                // Send a reply to the client
                String[] parts = message.split(" ");
                String infoType = parts[0];
                socket.send(STR."\{infoType} received", 0);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
