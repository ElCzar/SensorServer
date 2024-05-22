package com.forest.server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QualitySystem {
    public static void main(String[] args) {
        try (ZMQ.Context context = ZMQ.context(1)) {
            ZMQ.Socket socket = context.socket(SocketType.REP);
            socket.bind("tcp://*:5560");
            System.out.println("Quality System is running...");

            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();

                String[] parts = message.split(" ");
                System.out.println(STR."Warning detected: \n\tType: \{parts[1]} \n\tReading: \{parts[2]} \n\tTime: \{parts[3]}");

                socket.send(STR."Warning for \{parts[1]} published");
            }
        } catch (Exception e) {
            System.out.println("Error creating socket: " + e.getMessage());
            System.exit(1);
        }
    }
}
