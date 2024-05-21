package com.forest.server.proxy;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

public class ProxyServer {
    private static final double MAX_TEMPERATURE = 29.4; // Ajusta este valor al máximo de temperatura permitido
    private static final int SENSOR_COUNT = 10;
    private static Queue<Double> temperatureReadings = new LinkedList<>();
    private static Queue<Double> humidityReadings = new LinkedList<>();

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://*:5555");

            System.out.println("Listening...");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String message = new String(reply, ZMQ.CHARSET);
                System.out.println(STR."Received: [\{message}]");

                String[] parts = message.split(" ");
                String sensorType = parts[0];
                double reading = Double.parseDouble(parts[1]);

                switch (sensorType) {
                    case "Temperature":
                        receiveTemperature(reading);
                        break;
                    case "Humidity":
                        receiveHumidity(reading);
                        break;
                    default:
                        System.out.println(STR."Unknown sensor type: \{sensorType}");
                        break;
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println(STR."Error: \{e.getMessage()}");
        }
    }

    public static void receiveTemperature(double temperature) {
        if (temperatureReadings.size() >= SENSOR_COUNT) {
            temperatureReadings.remove();
        }
        temperatureReadings.add(temperature);

        System.out.println(STR."Received temperature: \{temperature}"); // Imprimir cada lectura de temperatura

        double averageTemperature = temperatureReadings.stream().mapToDouble(val -> val).average().orElse(0.0);
        System.out.println(STR."Average temperature: \{averageTemperature} at \{LocalDateTime.now()}"); // Imprimir el promedio de temperatura

        if (averageTemperature > MAX_TEMPERATURE) {
            sendWarningToQualityControl(STR."Temperature exceeded maximum limit: \{averageTemperature}");
            sendWarningToCloud(STR."Temperature exceeded maximum limit: \{averageTemperature}");
        }
    }

    public static void receiveHumidity(double humidity) {
        if (humidityReadings.size() >= SENSOR_COUNT) {
            humidityReadings.remove();
        }
        humidityReadings.add(humidity);

        System.out.println("Received humidity: " + humidity); // Imprimir cada lectura de humedad

        double averageHumidity = humidityReadings.stream().mapToDouble(val -> val).average().orElse(0.0);
        System.out.println("Average humidity: " + averageHumidity + " at " + LocalDateTime.now()); // Imprimir el promedio de humedad

        sendHumidityToCloud(averageHumidity);
    }

    private static void sendWarningToQualityControl(String message) {
        // Implementa este método para enviar una alerta al sistema de control de calidad
    }

    private static void sendWarningToCloud(String message) {
        // Implementa este método para enviar un SMS, correo, etc. a la nube
    }

    private static void sendHumidityToCloud(double humidity) {
        // Implementa este método para enviar la humedad promedio a la capa cloud
    }
}