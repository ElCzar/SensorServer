package com.forest.server.sensors;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class AddressListener implements Runnable {
    private static final String healthCheckAddress = "tcp://10.43.100.44:5824";
    private static final String originalAddress = "tcp://localhost:5555";
    private ZMQ.Socket socketHealthCheck;
    private ZMQ.Socket socket;
    private ZContext context;

    public AddressListener() {
        context = new ZContext();
        try {
            socketHealthCheck = context.createSocket(SocketType.SUB);
            socketHealthCheck.connect(healthCheckAddress);  // Connect to the health check publisher
            socketHealthCheck.subscribe("".getBytes());

            socket = context.createSocket(SocketType.PUSH);
            socket.connect(originalAddress);
        } catch (Exception e) {
            System.out.println(STR."Error initializing sockets: \{e.getMessage()}");
            cleanup();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String message = socketHealthCheck.recvStr();
                if (message != null && !message.isEmpty()) {
                    socket.disconnect(originalAddress);
                    socket.connect(message);
                    System.out.println(STR."Connected to new address at \{message}");
                }
            }
        } catch (Exception e) {
            System.out.println(STR."Error receiving message: \{e.getMessage()}");
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (socketHealthCheck != null) {
            socketHealthCheck.close();
        }
        if (socket != null) {
            socket.close();
        }
        if (context != null) {
            context.close();
        }
    }

    public ZMQ.Socket getSocket() {
        return socket;
    }
}
