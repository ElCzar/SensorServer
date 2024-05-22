package com.forest.server;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.Scanner;

public class HealthChecker {


    public static void main(String[] args) {
        // Creates a publisher type socket
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUB);
            socket.bind("tcp://*:5540");
            System.out.println("Health Checker is running...");

            // Use a scan for purposes of demonstration
            Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Press enter to change the status of the health checker.");
                String message = scanner.nextLine();
                socket.send("tcp://localhost:5555", ZMQ.SNDMORE);
            }
        } catch (Exception e) {
            System.out.println(STR."Error creating socket: \{e.getMessage()}");
            System.exit(1);
        }
    }
}
