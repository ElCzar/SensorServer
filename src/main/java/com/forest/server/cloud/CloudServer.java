package com.forest.server.cloud;

import com.forest.server.SystemData;
import com.forest.server.proxy.MetricsProxy;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class CloudServer {
    public static final PersistencyCloud persistencyCloud = new PersistencyCloud();

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            // Create a ZeroMQ reply socket
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5556");
            CalculatorCloud calculatorCloud = new CalculatorCloud();
            calculatorCloud.run();

            System.out.println("Listening...");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                // Increment metric counter
                MetricsProxy.prometheusRegistry.counter("received_requests").increment();

                String message = new String(reply, ZMQ.CHARSET);
                System.out.println("Received: [" + message + "]");

                // Save to persistency
                persistencyCloud.clasifyAndSaveData(message);

                // Send a reply to the client
                String[] parts = message.split(" ");
                String infoType = parts[0];
                String sensorType = parts[1];

                if (infoType.equals(SystemData.AVERAGE) && sensorType.equals(SystemData.HUMIDITY)) {
                    calculatorCloud.calculateAverageHumidity(Double.parseDouble(parts[2]));
                }

                socket.send(STR."\{infoType} received", 0);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
