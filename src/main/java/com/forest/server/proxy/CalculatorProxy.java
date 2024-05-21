package com.forest.server.proxy;

import com.forest.server.SystemData;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorProxy {
    private final ExecutorService executorService;
    private final String cloudAddress = "tcp://localhost:5556";
    private static final ArrayList<Double> temperatureReadings = new ArrayList<>();
    private static final ArrayList<Double> humidityReadings = new ArrayList<>();

    private static final Double minTemperature = 11.0;
    private static final Double maxTemperature = 29.4;

    public CalculatorProxy() {
        this.executorService = Executors.newCachedThreadPool();
    }

    public void receiveTemperature(double temperature) {
        executorService.submit(() -> {
            if (temperatureReadings.size() > SystemData.SENSOR_COUNT) {
                temperatureReadings.clear();
            }
            temperatureReadings.add(temperature);

            // If is not negative value send reading to cloud
            if (temperature > 0) {
                sendMessageToCloud(STR."\{SystemData.MEASUREMENT} \{SystemData.TEMPERATURE} \{temperature}");
            }

            if (temperatureReadings.size() == SystemData.SENSOR_COUNT) {
                double promedio = temperatureReadings.stream().filter(a -> a>0).mapToDouble(Double::doubleValue).average().orElse(0);
                System.out.println(STR."Promedio temperatura \{LocalDate.now()}: \{promedio}");
                // Send average to cloud
                sendMessageToCloud(STR."\{SystemData.AVERAGE} \{SystemData.TEMPERATURE} \{promedio}");

                // If the average temperature is out of range, detect a warning
                if (promedio < minTemperature || promedio > maxTemperature) {
                    warningDetected(STR."\{SystemData.WARNING} \{SystemData.TEMPERATURE} \{promedio}");
                }
            }
        });
    }

    public void receiveHumidity(double humidity) {
        executorService.submit(() -> {
            if (humidityReadings.size() >= SystemData.SENSOR_COUNT) {
                humidityReadings.remove(0);
            }
            humidityReadings.add(humidity);

            if (humidity > 0) {
                sendMessageToCloud(STR."\{SystemData.MEASUREMENT} \{SystemData.HUMIDITY} \{humidity}");
            }

            if (humidityReadings.size() == SystemData.SENSOR_COUNT) {
                double promedio = humidityReadings.stream().filter(a -> a>0).mapToDouble(Double::doubleValue).average().orElse(0);
                System.out.println(STR."Promedio humedad \{LocalDate.now()}: \{promedio}");
            }
        });
    }

    public void receiveFog(double fog) {
        executorService.submit(() -> {
            if (fog > 0) {
                sendMessageToCloud(STR."\{SystemData.MEASUREMENT} \{SystemData.FOG} \{fog}");
            }
        });
    }

    public void warningDetected(String message) {
        executorService.submit(() -> {
            sendWarningToQualityControl(message);
            sendMessageToCloud(message);
        });
    }

    public void sendMessageToCloud(String message) {
        executorService.submit(() -> {
            // Create a connection of request to the cloud
            try (ZContext context = new ZContext()) {
                ZMQ.Socket socket = context.createSocket(SocketType.REQ);
                socket.connect(cloudAddress);
                socket.send(message);

                byte[] reply = socket.recv(0);
                System.out.println(STR."Success: [\{new String(reply, ZMQ.CHARSET)}]");
            }
        });
    }

    private void sendWarningToQualityControl(String message) {
        // TODO Implementa este método para enviar una alerta al sistema de control de calidad - REQUEST-REPLY
        executorService.submit(() -> {
            System.out.println(STR."Sending warning to quality control: \{message}");
        });
    }
}
