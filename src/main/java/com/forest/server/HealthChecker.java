package com.forest.server;

import com.forest.server.proxy.ProxyServer;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.Scanner;

public class HealthChecker {
    private static final String originalAddress = "tcp://10.43.101.8:5540";

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            // Socket for publishing proxy health
            ZMQ.Socket socket = context.createSocket(SocketType.PUB);
            socket.bind("tcp://*:5824");
            System.out.println("Health Checker is running...");
            // Socket for making the proxy requests
            ZMQ.Socket requestSocket = context.createSocket(SocketType.REQ);
            requestSocket.connect(originalAddress);
            // Poller for responses
            ZMQ.Poller poller = context.createPoller(1);
            poller.register(requestSocket, ZMQ.Poller.POLLIN);

            while (!Thread.currentThread().isInterrupted()) {
                // Make a request to the proxy server if it doesnt responds in 250ms then is timeout
                requestSocket.send("Health Check");
                int responseReceived = poller.poll(250);

                if (responseReceived == 1) {
                    String message = requestSocket.recvStr();
                    System.out.println("Received response from proxy: " + message);
                } else {
                    System.out.println("Proxy is not responding...");
                    socket.send("tcp://10.43.100.44:5555");
                    ProxyServer.main(new String[]{});
                }
            }

        } catch (Exception e) {
            System.out.println(STR."Error creating socket: \{e.getMessage()}");
            System.exit(1);
        }
    }
}
