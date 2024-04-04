package com.forest.server;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ProxyServer {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://*:5555");

            System.out.println("Listening...");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                System.out.println(STR."Received: [\{new String(reply, ZMQ.CHARSET)}]");
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println(STR."Error: \{e.getMessage()}");
        }
    }
}
