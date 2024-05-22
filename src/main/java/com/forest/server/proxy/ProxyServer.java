package com.forest.server.proxy;

import com.forest.server.SystemData;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
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

            // Creates hearbeat
            Hearbeat hearbeat = new Hearbeat();
            Thread hearbeatThread = new Thread(hearbeat);
            hearbeatThread.start();
            System.out.println("Listening...");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                // Increment metric counter
                Metrics.prometheusRegistry.counter("request").increment();

                String message = new String(reply, ZMQ.CHARSET);
                System.out.println(STR."Received: [\{message}]");

                String[] parts = message.split(" ");
                String solicitudeType = parts[0];
                double reading = 0;
                String time = "";
                if (!solicitudeType.equals(SystemData.WARNING)) {
                    reading = Double.parseDouble(parts[1]);
                     time = parts[2];
                }

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
        }
    }


}