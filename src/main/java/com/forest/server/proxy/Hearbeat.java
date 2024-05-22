package com.forest.server.proxy;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Hearbeat implements Runnable{
    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5540");
            System.out.println("Heartbeat is running...");
            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();
                socket.send("Heartbeat");
                // Increment hearbeat metric counter
                MetricsProxy.prometheusRegistry.counter("heartbeat").increment();
            }
        } catch (Exception e) {
            System.out.println("Error creating socket for hearbeat: " + e.getMessage());
            System.exit(1);
        }
    }

}
