package com.forest.server.proxy;

import com.forest.server.SystemData;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ProxyServer {

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://*:5555");
            CalculatorProxy calculatorProxy = new CalculatorProxy();

            System.out.println("Listening...");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String message = new String(reply, ZMQ.CHARSET);
                System.out.println(STR."Received: [\{message}]");

                String[] parts = message.split(" ");
                String sensorType = parts[0];
                double reading = Double.parseDouble(parts[1]);

                switch (sensorType) {
                    case SystemData.TEMPERATURE:
                        calculatorProxy.receiveTemperature(reading);
                        break;
                    case SystemData.HUMIDITY:
                        calculatorProxy.receiveHumidity(reading);
                        break;
                    case SystemData.FOG:
                        calculatorProxy.receiveFog(reading);
                        break;
                    case SystemData.WARNING:
                        calculatorProxy.warningDetected(message);
                        break;
                    default:
                        System.out.println(STR."Unknown information type: \{sensorType}");
                        break;
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println(STR."Error: \{e.getMessage()}");
        }
    }
}