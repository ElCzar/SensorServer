package com.forest.server.sensors;

import com.forest.server.SystemData;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Sprinkler implements Runnable{
    // Uses push pull to get any messages from the Fog Sensor
    @Override
    public void run() {
        // Create a socket to receive messages from the Fog Sensor
        try (ZContext context = new ZContext(1)) {
            ZMQ.Socket socket = context.createSocket(SocketType.PULL);
            socket.connect("tcp://localhost:5590");

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Receive the message from the Fog Sensor
                    String message = socket.recvStr();
                    System.out.println(message);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            System.out.println("Error creating socket: " + e.getMessage());
            System.exit(1);
        }
    }
}
