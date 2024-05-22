package com.forest.server.sensors;


import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class AddressListener implements Runnable{
    private final String healthCheck = "tcp://10.43.100.44:5540";
    private final String originalAddress = "tcp://localhost:5555";
    private ZMQ.Socket socketHealthCheck;
    private ZMQ.Socket socket;

    public AddressListener() {
        // Create a socket
        try (ZContext context = new ZContext(1)) {
            socketHealthCheck = context.createSocket(SocketType.SUB);
            socketHealthCheck.bind(healthCheck);
        } catch (Exception e) {
            System.out.println(STR."Error creating health check socket: \{e.getMessage()}");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        // Creates the socket for the sensor
        try (ZContext context = new ZContext(1)) {
            socket = context.createSocket(SocketType.PUSH);
            socket.connect(originalAddress);
        } catch (Exception e) {
            System.out.println(STR."Error creating socket: \{e.getMessage()}");
            System.exit(1);
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String message = socketHealthCheck.recvStr();
                if (!message.isEmpty()) {
                    System.out.println("Proxy is down, changing address...");
                    socket.disconnect(originalAddress);
                    socket.connect(message);
                }
            } catch (Exception e) {
                System.out.println(STR."Error receiving message: \{e.getMessage()}");
                Thread.currentThread().interrupt();
            }
        }
    }

    public ZMQ.Socket getSocket() {
        return socket;
    }
}
