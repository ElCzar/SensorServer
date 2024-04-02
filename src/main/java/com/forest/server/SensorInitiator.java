package com.forest.server;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SensorInitiator {
    public static final int SENSOR_COUNT = 10;
    public static final String address = "tcp://localhost:5555";

    public void initiateSensors(String type, String file) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUSH);
            socket.connect(address);

            List<Double> data = readDataFromFile(file);
            Double probabilityCorrect = data.get(0);
            Double probabilityOutOfRange = data.get(1);
            Double probabilityError = data.get(2);


            for (int i = 0; i < SENSOR_COUNT; i++) {
                Sensor sensor = switch (type) {
                    case SensorServer.TEMPERATURE -> new TemperatureSensor(socket, probabilityCorrect, probabilityOutOfRange, probabilityError);
                    //case SensorServer.HUMIDITY -> new HumiditySensor(socket, 0.8, 0.1, 0.1);
                    //case SensorServer.FOG -> new FogSensor(socket, 0.8, 0.1, 0.1);
                    default -> null;
                };

                if (sensor != null) {
                    sensor.start();
                }
            }
        }
    }

    public List<Double> readDataFromFile(String file) {
        // Array list with 3 elements in -1
        List<Double> data = new ArrayList<>(List.of(-1.0, -1.0, -1.0));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");

                String type = parts[0];
                Double value = Double.parseDouble(parts[1]);

                switch (type) {
                    case "C" -> data.set(0, value);
                    case "O" -> data.set(1, value);
                    case "E" -> data.set(2, value);
                    default -> throw new IllegalArgumentException(STR."Invalid probability type: \{type}");
                }
            }

        } catch (Exception e) {
            System.out.println(STR."Error reading file: \{e.getMessage()}");
            throw new IllegalArgumentException(STR."Error reading file: \{e.getMessage()}");
        }

        if (data.get(0) == null || data.get(1) == null || data.get(2) == null) {
            System.out.println("Error reading file: Not enough data");
            throw new IllegalArgumentException("Error reading file: Not enough data");
        }

        return data;
    }
}
