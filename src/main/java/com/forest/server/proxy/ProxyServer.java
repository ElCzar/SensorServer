package com.forest.server.proxy;

import com.forest.server.SystemData;
import io.micrometer.core.instrument.Timer;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.TimeUnit;

public class ProxyServer {

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://*:5555");
            CalculatorProxy calculatorProxy = new CalculatorProxy();

            // Creates hearbeat
            Hearbeat hearbeat = new Hearbeat();
            Thread hearbeatThread = new Thread(hearbeat);
            hearbeatThread.start();
            System.out.println("Listening...");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                // Detects a message arrived
                long endTime = System.currentTimeMillis();

                // Gets message
                String message = new String(reply, ZMQ.CHARSET);
                String[] parts = message.split(" ");
                System.out.println(STR."Received: [\{message}]");

                String solicitudeType = parts[0];
                double reading = 0;
                String time = "";
                long startTime = 0;
                if (!solicitudeType.equals(SystemData.WARNING)) {
                    reading = Double.parseDouble(parts[1]);
                    time = parts[2];
                    startTime = Long.parseLong(parts[3]);
                } else {
                    startTime = Long.parseLong(parts[4]);
                }

                // Increment metric counter
                MetricsProxy.prometheusRegistry.counter("received_requests").increment();
                // Set timer for receive time
                Timer timer = Timer.builder("proxy_response_time")
                        .description("Time taken to get the push from the sensor")
                        .register(MetricsProxy.prometheusRegistry);
                timer.record(endTime - startTime, TimeUnit.MILLISECONDS);

                switch (solicitudeType) {
                    case SystemData.TEMPERATURE:
                        calculatorProxy.receiveTemperature(reading, time);
                        break;
                    case SystemData.HUMIDITY:
                        calculatorProxy.receiveHumidity(reading, time);
                        break;
                    case SystemData.FOG:
                        calculatorProxy.receiveFog(reading, time);
                        break;
                    case SystemData.WARNING:
                        calculatorProxy.warningDetected(message);
                        break;
                    default:
                        System.out.println(STR."Unknown information type: \{solicitudeType}");
                        break;
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println(STR."Error: \{e.getMessage()}");
            e.printStackTrace();
        }
    }


}